#!/usr/bin/env python3
"""Builds the listing scenes with server commands and captures them from the QA client (see marketing/README.md).

Run against a 26.3 Fabric dev server whose world is the showcase world (level-name=showcase, level-type
minecraft:normal, level-seed SEED below, broadcast-rcon-to-ops=false), with the QA client online and not op'd:

  python3 tools/qa/marketing_shots.py --base X,Y,Z [scene...]

BASE is the first air block above flat ground with a river a few blocks to the south (31,64,-62 in the showcase
seed). Every scene is built relative to it: the cottage to the north, the quarry to the west, the bridge over the
river to the south. The player's block reach is raised so the shots can be framed from further away. Without scenes,
all of them are taken.
"""
from __future__ import annotations

import argparse
import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from client import Session, PLAYER  # noqa: E402

SEED = '7214950366214'
NS = 'reconstructedwands'
OUT = Path(__file__).resolve().parents[2] / 'marketing' / 'images' / 'screenshots'


def wand(tier='diamond', cores=('angel', 'destruction', 'exchange', 'architect'), sel=0, extra=''):
    core_list = ','.join(f'"{NS}:core_{c}"' for c in cores)
    options = f'cores:[{core_list}],cores_sel:{sel}b' + (',' + extra if extra else '')
    return f'{NS}:{tier}_wand[minecraft:custom_data={{wand_options:{{{options}}}}}]'


def shulker(color, block, stacks=9):
    # RCON requests are limited to about 1400 bytes, so a full box would not fit in one command.
    items = ','.join(f'{{slot:{i},item:{{id:"{block}",count:64}}}}' for i in range(stacks))
    return f'minecraft:{color}_shulker_box[minecraft:container=[{items}]]'


class Scenes:
    def __init__(self, s: Session, base):
        self.s = s
        self.x, self.y, self.z = base
        self.recipes_known = False

    # Helpers ---------------------------------------------------------------------------------------------------------

    def at(self, dx, dy, dz):
        return f'{self.x + dx} {self.y + dy} {self.z + dz}'

    def fill(self, a, b, block):
        # Large fills are split along x so each command stays under the 32768-block limit.
        (x1, y1, z1), (x2, y2, z2) = a, b
        lo, hi = min(x1, x2), max(x1, x2)
        step = max(1, 32000 // ((abs(y2 - y1) + 1) * (abs(z2 - z1) + 1)))
        for x in range(lo, hi + 1, step):
            self.s.cmd(f'fill {self.at(x, y1, z1)} {self.at(min(hi, x + step - 1), y2, z2)} {block}')

    def put(self, d, block):
        self.s.cmd(f'setblock {self.at(*d)} {block}')

    def tp(self, d, facing):
        self.s.tp(self.x + d[0], self.y + d[1], self.z + d[2], facing=(self.x + facing[0], self.y + facing[1], self.z + facing[2]))
        time.sleep(1.2)

    def hold(self, item, offhand='minecraft:air', hotbar=()):
        self.s.cmd(f'clear {PLAYER}')
        self.s.cmd(f'item replace entity {PLAYER} hotbar.0 with {item}')
        for i, extra in enumerate(hotbar, start=1):
            self.s.cmd(f'item replace entity {PLAYER} hotbar.{i} with {extra}')
        self.s.cmd(f'item replace entity {PLAYER} weapon.offhand with {offhand}')

    def shot(self, name, wait=1.5):
        time.sleep(wait)
        self.s.shot(OUT / name.replace('.jpg', '.png'))
        from PIL import Image
        png = OUT / name.replace('.jpg', '.png')
        Image.open(png).convert('RGB').save(OUT / name, quality=90)
        png.unlink()
        print('captured', name, flush=True)

    # The world -------------------------------------------------------------------------------------------------------

    def prepare(self):
        s = self.s
        s.cmd('gamerule advance_time false', 'gamerule advance_weather false', 'gamerule spawn_mobs false',
              'time set 1500', 'weather clear', 'difficulty peaceful', f'gamemode survival {PLAYER}',
              f'attribute {PLAYER} minecraft:block_interaction_range base set 24', 'kill @e[type=!player]')
        # Level the plot north of the riverbank: grass on top, air above.
        self.fill((-26, 0, -26), (26, 24, 4), 'minecraft:air')
        self.fill((-26, -4, -26), (26, -2, 4), 'minecraft:dirt')
        self.fill((-26, -1, -26), (26, -1, 4), 'minecraft:grass_block')
        s.cmd('kill @e[type=item]')
        if not self.recipes_known:
            # Unlock every recipe once, so no "New recipes unlocked" toast lands in a shot.
            s.cmd(f'recipe give {PLAYER} *')
            self.recipes_known = True
            time.sleep(8)

    def cottage(self, rows=3):
        """An 11 x 9 plank cottage north of BASE on a stone-brick footing, `rows` rows high, with a door and windows."""
        x1, x2, z1, z2 = -5, 5, -16, -8
        self.fill((x1 - 1, -1, z1 - 1), (x2 + 1, -1, z2 + 1), 'minecraft:stone_bricks')
        self.fill((x1, -1, z1), (x2, -1, z2), 'minecraft:spruce_planks')
        for dy in range(rows):
            block = 'minecraft:cobblestone' if dy == 0 else 'minecraft:oak_planks'
            self.fill((x1, dy, z1), (x2, dy, z2), block)
            self.fill((x1 + 1, dy, z1 + 1), (x2 - 1, dy, z2 - 1), 'minecraft:air')
        # Door and windows.
        self.fill((0, 0, z2), (0, 1, z2), 'minecraft:air')
        self.put((0, 0, z2), 'minecraft:spruce_door[half=lower,facing=south]')
        self.put((0, 1, z2), 'minecraft:spruce_door[half=upper,facing=south]')
        if rows > 1:
            for wx in (-3, 3):
                self.put((wx, 1, z2), 'minecraft:glass_pane[east=true,west=true]')
            for wz in (-14, -11):
                self.put((x1, 1, wz), 'minecraft:glass_pane[north=true,south=true]')
                self.put((x2, 1, wz), 'minecraft:glass_pane[north=true,south=true]')
        # A lantern post and flowers by the path.
        self.fill((0, -1, -7), (0, -1, 2), 'minecraft:dirt_path')
        for dz, flower in ((-6, 'minecraft:poppy'), (-4, 'minecraft:cornflower'), (-2, 'minecraft:oxeye_daisy')):
            self.put((-1, 0, dz), flower)
            self.put((1, 0, dz + 1), 'minecraft:dandelion')
        self.fill((-3, 0, -6), (-3, 1, -6), 'minecraft:spruce_fence')
        self.put((-3, 2, -6), 'minecraft:lantern')

    def stand(self, d, height):
        """A log pillar to stand on (scaffolding would drop the player when they sneak)."""
        self.fill((d[0], 0, d[2]), (d[0], height - 1, d[2]), 'minecraft:spruce_log')

    # Scenes ----------------------------------------------------------------------------------------------------------

    def build(self):
        """Construction: the next row of the whole wall ring, previewed from a pillar outside the south-east corner."""
        self.cottage(rows=3)
        self.stand((8, 0, -5), 3)
        self.hold(wand(), hotbar=('minecraft:oak_planks 64', shulker('orange', 'minecraft:oak_planks', 9),
                                  shulker('light_blue', 'minecraft:cobblestone', 9), 'minecraft:stone_bricks 64'))
        # Aim at the top face of the front wall: the next row is previewed all the way round.
        self.tp((8.5, 3, -4.5), facing=(3.5, 3.0, -7.6))
        self.shot('build.jpg')

    def undo(self):
        """Holding Ctrl and sneaking after building the row: the undo preview."""
        self.cottage(rows=4)
        self.stand((8, 0, -5), 3)
        self.hold(wand(), hotbar=('minecraft:oak_planks 64', 'minecraft:oak_planks 64'))
        self.s.cmd(f'fill {self.at(-5, 3, -16)} {self.at(5, 3, -8)} minecraft:air')
        self.tp((8.5, 3, -4.5), facing=(3.5, 3.0, -7.6))
        self.s.click(3)
        time.sleep(1.5)
        self.tp((8.5, 3, -4.5), facing=(3.5, 4.0, -7.6))
        self.s.xdo('keydown', 'ctrl')
        self.s.xdo('keydown', 'shift')
        self.shot('undo.jpg', wait=1.0)
        self.s.xdo('keyup', 'shift')
        self.s.xdo('keyup', 'ctrl')

    def exchange(self):
        """Exchange: a cobblestone garden wall, half of it already stone bricks, the rest previewed."""
        self.cottage(rows=4)
        self.fill((-16, 0, -3), (-4, 2, -3), 'minecraft:cobblestone')
        self.fill((-16, 0, -3), (-11, 2, -3), 'minecraft:stone_bricks')
        self.put((-10, 1, -3), 'minecraft:stone_bricks')
        self.fill((-16, 3, -3), (-4, 3, -3), 'minecraft:stone_brick_slab')
        self.hold(wand(sel=3), offhand='minecraft:stone_bricks 64')
        self.tp((-11.5, 0, 4.0), facing=(-6.5, 1.5, -2.95))
        self.shot('exchange.jpg')

    def destruction(self):
        """Destruction: a stone cliff face with ores, cleared with a diamond pickaxe in the offhand."""
        self.fill((-25, 0, -22), (-13, 7, -10), 'minecraft:stone')
        self.fill((-25, 8, -22), (-13, 8, -12), 'minecraft:dirt')
        self.fill((-25, 9, -22), (-13, 9, -14), 'minecraft:grass_block')
        self.fill((-25, 8, -11), (-13, 8, -11), 'minecraft:grass_block')
        for d, block in (((-21, 2, -10), 'minecraft:coal_ore'), ((-20, 2, -10), 'minecraft:coal_ore'), ((-20, 3, -10), 'minecraft:coal_ore'),
                         ((-16, 5, -10), 'minecraft:iron_ore'), ((-23, 6, -10), 'minecraft:andesite'), ((-24, 6, -10), 'minecraft:andesite'),
                         ((-15, 1, -10), 'minecraft:granite'), ((-14, 1, -10), 'minecraft:granite'), ((-14, 2, -10), 'minecraft:granite')):
            self.put(d, block)
        self.hold(wand(sel=2, extra='match:2'), offhand='minecraft:diamond_pickaxe')
        # Aim at plain stone: the ores are in #reconstructedwands:no_dismantle and stay.
        self.tp((-18.5, 0, 0.5), facing=(-18.5, 3.5, -9.95))
        self.shot('destruction.jpg')

    def angel(self):
        """Angel: a sky walk high over the river, the next block previewed in mid-air."""
        self.s.cmd(f'attribute {PLAYER} minecraft:block_interaction_range base reset')
        h = 14
        self.fill((-1, h, 2), (1, h, 4), 'minecraft:spruce_planks')
        for z in range(5, 10):
            self.put((0, h, z), 'minecraft:spruce_planks')
        self.put((1, h + 1, 4), 'minecraft:lantern')
        self.hold(wand(sel=1), hotbar=('minecraft:spruce_planks 64',))
        # At the end of the walk, looking ahead and down: the block two blocks ahead is the next plank.
        self.s.tp(self.x + 0.5, self.y + h + 1, self.z + 9.85, yaw=0, pitch=36)
        time.sleep(1.5)
        self.shot('angel.jpg')
        self.s.cmd(f'attribute {PLAYER} minecraft:block_interaction_range base set 24')

    def architect(self):
        """Architect: a ring beside the cottage; the first click is its centre and the aim sets the radius."""
        self.cottage(rows=4)
        self.stand((13, 0, 3), 6)
        self.hold(wand(sel=4, extra='shape:6'), offhand='minecraft:stone_bricks 64', hotbar=('minecraft:stone_bricks 64',))
        self.tp((13.5, 6, 3.5), facing=(13.5, -0.05, -6.5))
        self.s.click(3)
        time.sleep(1.0)
        self.tp((13.5, 6, 3.5), facing=(18.5, -0.05, -6.5))
        self.shot('architect.jpg')

    def supply(self):
        """A chest bound to the wand, shulker boxes in the hotbar and the HUD count."""
        self.cottage(rows=3)
        self.put((7, 0, -4), 'minecraft:chest[facing=west]')
        for slot in range(27):
            self.s.cmd(f'item replace block {self.at(7, 0, -4)} container.{slot} with minecraft:oak_planks 64')
        self.hold(wand(), hotbar=(shulker('orange', 'minecraft:oak_planks'), shulker('lime', 'minecraft:oak_planks'),
                                  shulker('purple', 'minecraft:oak_planks'), 'minecraft:oak_planks 64'))
        self.tp((9.5, 0, -2.5), facing=(7.5, 0.5, -3.5))
        self.s.xdo('keydown', 'shift')
        time.sleep(0.5)
        self.s.click(3)
        self.s.xdo('keyup', 'shift')
        time.sleep(0.3)
        self.tp((9.5, 0, -2.5), facing=(5.5, 2.5, -7.95))
        self.shot('supply.jpg', wait=0.6)

    def screens(self):
        """The settings screen and the mode wheel, over the cottage."""
        self.cottage(rows=3)
        self.hold(wand(extra='shape:6'), hotbar=('minecraft:oak_planks 64',))
        self.tp((0.5, 0, 0.5), facing=(0.5, 1.5, -7.95))
        self.s.key('g', delay=1.5)
        self.shot('screen.jpg', wait=0.5)
        self.s.key('Escape', delay=0.6)
        self.s.xdo('keydown', 'r')
        time.sleep(1.2)
        self.s.move(960 + 150, 540 - 50)
        time.sleep(0.8)
        self.shot('wheel.jpg', wait=0.2)
        self.s.xdo('keyup', 'r')


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--base', required=True, help='x,y,z of the ground spot every scene is built around')
    ap.add_argument('--port', type=int, default=25575)
    ap.add_argument('--no-prepare', action='store_true', help='keep the plot as it is')
    ap.add_argument('scenes', nargs='*')
    args = ap.parse_args()
    s = Session(port=args.port)
    scenes = Scenes(s, tuple(int(v) for v in args.base.split(',')))
    s.focus()
    s.hud(True)
    wanted = args.scenes or ['build', 'undo', 'supply', 'exchange', 'destruction', 'angel', 'architect', 'screens']
    for name in wanted:
        if not args.no_prepare:
            scenes.prepare()
        getattr(scenes, name)()
    return 0


if __name__ == '__main__':
    sys.exit(main())

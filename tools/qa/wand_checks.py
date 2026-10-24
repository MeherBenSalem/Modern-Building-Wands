#!/usr/bin/env python3
"""In-game checks with a real QA client online (see devrun.sh): every mode in survival, undo and redo, the size and
lock settings, shulker box, Ender Chest and bound-chest supply, the deny tag, config reload, and captures of the HUD,
the settings screen and the mode wheel. The checks build around x -8..8, z 0..14 of a superflat world.

  python3 tools/qa/wand_checks.py --mc 26.3 --out /tmp/wands-qa/shots
"""
from __future__ import annotations

import argparse
import re
import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from client import Session, PLAYER  # noqa: E402

FAILURES = []
Y = -60  # first air layer of a default superflat world
NS = 'reconstructedwands'


def check(name, ok, detail=''):
    print(('PASS ' if ok else 'FAIL ') + name + (f'  [{str(detail)[:300]}]' if detail and not ok else ''), flush=True)
    if not ok:
        FAILURES.append(name)


class Wands:
    def __init__(self, s: Session, mc: str):
        self.s = s
        self.legacy = mc == '1.20.1'
        # 26.x renamed doDaylightCycle to advance_time.
        self.daylight_rule = 'doDaylightCycle' if mc.startswith('1.') else 'advance_time'

    def wand(self, tier='iron', cores=(), sel=0, extra=''):
        """An item argument for a wand with cores installed and the given mode index selected."""
        core_list = ','.join(f'"{NS}:core_{c}"' for c in cores)
        options = f'cores:[{core_list}],cores_sel:{sel}b' + (',' + extra if extra else '')
        if self.legacy:
            return f'{NS}:{tier}_wand{{wand_options:{{{options}}}}}'
        return f'{NS}:{tier}_wand[minecraft:custom_data={{wand_options:{{{options}}}}}]'

    def hold(self, item, offhand=None):
        # The wand goes in hotbar slot 0 (selected); everything else is given afterwards into slots 1+.
        self.s.cmd(f'item replace entity {PLAYER} hotbar.0 with {item}')
        self.s.cmd(f'item replace entity {PLAYER} weapon.offhand with {offhand or "minecraft:air"}')

    def give(self, item):
        self.s.cmd(f'give {PLAYER} {item}')

    def count(self, item):
        out = self.s.cmd(f'clear {PLAYER} {item} 0')
        m = re.search(r'(\d+)', out)
        return int(m.group(1)) if m and 'No items' not in out else 0

    def block(self, x, y, z, block):
        return 'passed' in self.s.cmd(f'execute if block {x} {y} {z} {block}').lower()

    def blocks(self, positions, block):
        return sum(1 for p in positions if self.block(*p, block))

    def reset(self):
        self.s.cmd(f'fill -8 {Y} -2 8 {Y + 10} 14 minecraft:air', f'clear {PLAYER}', f'gamemode survival {PLAYER}',
                   f'item replace entity {PLAYER} hotbar.0 with minecraft:barrier',
                   f'gamerule {self.daylight_rule} false', 'time set noon', 'weather clear')
        time.sleep(0.4)

    def wall(self, block='minecraft:stone', z=8):
        self.s.cmd(f'fill -1 {Y} {z} 1 {Y + 2} {z} {block}')

    def face_wall(self, z=8):
        """Stand at z=4.5 looking at the centre of the wall's north face."""
        self.s.tp(0.5, Y, 4.5, facing=(0.5, Y + 1.5, z - 0.01))
        time.sleep(0.8)

    def use(self, wait=1.0):
        self.s.click(3)
        time.sleep(wait)


def layer(z):
    return [(x, y, z) for x in (-1, 0, 1) for y in (Y, Y + 1, Y + 2)]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--mc', default='26.3')
    ap.add_argument('--out', default='/tmp/wands-qa/shots')
    ap.add_argument('--port', type=int, default=25575)
    args = ap.parse_args()
    out = Path(args.out)
    s = Session(port=args.port)
    w = Wands(s, args.mc)
    s.focus()
    s.hud(True)

    # Construction in survival: a 3x3 stone wall grows one layer towards the player.
    w.reset()
    w.wall()
    w.hold(w.wand('iron'))
    s.cmd(f'give {PLAYER} minecraft:stone 64')
    w.face_wall()
    s.shot(out / 'hud-construction.png')
    w.use()
    placed = w.blocks(layer(7), 'minecraft:stone')
    check('construction places the 3x3 layer', placed == 9, f'placed {placed}')
    check('construction takes 9 stone', w.count('minecraft:stone') == 55, w.count('minecraft:stone'))

    # Undo and redo with Ctrl+Z / Ctrl+Y.
    s.key('ctrl+z', delay=1.2)
    check('undo removes the layer', w.blocks(layer(7), 'minecraft:stone') == 0)
    check('undo returns the stone', w.count('minecraft:stone') == 64, w.count('minecraft:stone'))
    s.key('ctrl+y', delay=1.2)
    check('redo places it again', w.blocks(layer(7), 'minecraft:stone') == 9)
    check('redo takes the stone again', w.count('minecraft:stone') == 55, w.count('minecraft:stone'))

    # The 1.x gesture: hold Ctrl and sneak + right-click.
    s.xdo('keydown', 'ctrl')
    s.xdo('keydown', 'shift')
    time.sleep(0.6)
    w.use()
    s.xdo('keyup', 'shift')
    s.xdo('keyup', 'ctrl')
    time.sleep(0.5)
    check('ctrl + sneak + right-click undoes', w.blocks(layer(7), 'minecraft:stone') == 0)

    # Lock: Horizontal on a north face keeps to the middle row; size limits the count.
    w.reset()
    w.wall()
    s.cmd(f'give {PLAYER} minecraft:stone 64')
    w.hold(w.wand('iron', extra='lock:0'))
    w.face_wall()
    w.use()
    row = [(x, Y + 1, 7) for x in (-1, 0, 1)]
    check('horizontal lock builds one row', w.blocks(row, 'minecraft:stone') == 3 and w.blocks(layer(7), 'minecraft:stone') == 3)
    w.reset()
    w.wall()
    s.cmd(f'give {PLAYER} minecraft:stone 64')
    w.hold(w.wand('iron', extra='size:4'))
    w.face_wall()
    w.use()
    check('size 4 places four blocks', w.blocks(layer(7), 'minecraft:stone') == 4, w.blocks(layer(7), 'minecraft:stone'))

    # Shulker box supply: the only stone is inside a shulker box.
    w.reset()
    w.wall()
    if w.legacy:
        s.cmd(f'give {PLAYER} minecraft:shulker_box{{BlockEntityTag:{{Items:[{{Slot:0b,id:"minecraft:stone",Count:32b}}]}}}}')
    else:
        s.cmd(f'give {PLAYER} minecraft:shulker_box[minecraft:container=[{{slot:0,item:{{id:"minecraft:stone",count:32}}}}]]')
    w.hold(w.wand('iron'))
    w.face_wall()
    w.use()
    check('shulker box supplies the blocks', w.blocks(layer(7), 'minecraft:stone') == 9)
    inv = s.cmd(f'data get entity {PLAYER} Inventory')
    check('the shulker box gave exactly 9', re.search(r'(count|Count): 23b?', inv) is not None, inv[:300])

    # Ender Chest supply.
    w.reset()
    w.wall()
    s.cmd(f'item replace entity {PLAYER} enderchest.0 with minecraft:stone 20')
    w.hold(w.wand('iron'))
    w.face_wall()
    w.use()
    check('the Ender Chest supplies the blocks', w.blocks(layer(7), 'minecraft:stone') == 9)
    ender = s.cmd(f'data get entity {PLAYER} EnderItems')
    check('the Ender Chest gave exactly 9', re.search(r'(count|Count): 11b?', ender) is not None, ender[:300])
    s.cmd(f'item replace entity {PLAYER} enderchest.0 with minecraft:air')

    # Bound chest: sneak + right-click binds it, then it supplies the blocks.
    w.reset()
    w.wall()
    item_key = 'Count:64b' if w.legacy else 'count:64'
    s.cmd(f'setblock 3 {Y} 4 minecraft:chest{{Items:[{{Slot:0b,id:"minecraft:stone",{item_key}}}]}}')
    w.hold(w.wand('iron'))
    s.tp(2.5, Y, 2.5, facing=(3.5, Y + 0.6, 4.5))
    time.sleep(0.8)
    s.xdo('keydown', 'shift')
    time.sleep(0.5)
    w.use()
    s.xdo('keyup', 'shift')
    time.sleep(0.4)
    w.face_wall()
    w.use()
    check('a bound chest supplies the blocks', w.blocks(layer(7), 'minecraft:stone') == 9)
    chest = s.cmd(f'data get block 3 {Y} 4 Items')
    check('the chest gave exactly 9', re.search(r'(count|Count): 55b?', chest) is not None, chest[:300])

    # Destruction with a pickaxe in the offhand: the surface goes, cobblestone arrives.
    w.reset()
    w.wall()
    w.hold(w.wand('iron', cores=['destruction'], sel=1), offhand='minecraft:iron_pickaxe')
    w.face_wall()
    s.shot(out / 'hud-destruction.png')
    w.use()
    check('destruction removes the surface', w.blocks(layer(8), 'minecraft:air') == 9)
    check('destruction hands over the drops', w.count('minecraft:cobblestone') == 9, w.count('minecraft:cobblestone'))
    s.key('ctrl+z', delay=1.2)
    check('undoing destruction restores the wall', w.blocks(layer(8), 'minecraft:stone') == 9)
    check('undoing destruction takes the drops back', w.count('minecraft:cobblestone') == 0, w.count('minecraft:cobblestone'))

    # Exchange: the wall becomes planks, the stone comes back.
    w.reset()
    w.wall()
    w.hold(w.wand('iron', cores=['exchange'], sel=1), offhand='minecraft:oak_planks 32')
    w.face_wall()
    w.use()
    check('exchange swaps the surface', w.blocks(layer(8), 'minecraft:oak_planks') == 9)
    check('exchange returns the old blocks', w.count('minecraft:stone') == 9, w.count('minecraft:stone'))
    check('exchange takes the new blocks', w.count('minecraft:oak_planks') == 23, w.count('minecraft:oak_planks'))

    # Angel: a block in mid-air in front of the player.
    w.reset()
    s.cmd(f'give {PLAYER} minecraft:glass 4')
    w.hold(w.wand('iron', cores=['angel'], sel=1))
    s.tp(0.5, Y, 4.5, yaw=0, pitch=0)
    time.sleep(0.8)
    w.use()
    check('angel places a block in the air', w.count('minecraft:glass') == 3, w.count('minecraft:glass'))

    # Architect: two corners on the floor make a wall.
    w.reset()
    s.cmd(f'give {PLAYER} minecraft:bricks 64')
    w.hold(w.wand('diamond', cores=['architect'], sel=1, extra='shape:1'), offhand='minecraft:bricks 64')
    # Click the tops of two floor blocks within reach: the corners are the air blocks above them.
    s.tp(0.5, Y, 1.5, facing=(-1.5, Y - 0.01, 4.5))
    time.sleep(0.8)
    w.use()
    s.tp(0.5, Y, 1.5, facing=(2.5, Y - 0.01, 4.5))
    time.sleep(1.0)
    s.shot(out / 'architect-preview.png')
    w.use()
    wall = [(x, Y, 4) for x in range(-2, 3)]
    check('architect builds between the corners', w.blocks(wall, 'minecraft:bricks') == 5, w.blocks(wall, 'minecraft:bricks'))

    # The deny tag: bedrock is never removed.
    w.reset()
    w.wall('minecraft:bedrock')
    s.cmd(f'gamemode creative {PLAYER}')
    w.hold(w.wand('infinity', cores=['destruction'], sel=1))
    w.face_wall()
    w.use()
    check('denied blocks are never removed', w.blocks(layer(8), 'minecraft:bedrock') == 9)
    s.cmd(f'gamemode survival {PLAYER}')

    # Config reload.
    reply = s.cmd('wands reload')
    check('/wands reload', 'reload' in reply.lower(), reply)

    # Screens.
    w.reset()
    w.wall()
    w.hold(w.wand('diamond', cores=['angel', 'destruction', 'exchange', 'architect']))
    w.face_wall()
    s.key('g', delay=1.2)
    s.shot(out / 'screen.png')
    s.key('Escape', delay=0.6)
    s.xdo('keydown', 'r')
    time.sleep(1.2)
    s.move(960 + 120, 540 - 60)
    time.sleep(0.6)
    s.shot(out / 'wheel.png')
    s.xdo('keyup', 'r')
    time.sleep(0.8)

    print(f'{len(FAILURES)} failed' if FAILURES else 'all checks passed')
    return 1 if FAILURES else 0


if __name__ == '__main__':
    sys.exit(main())

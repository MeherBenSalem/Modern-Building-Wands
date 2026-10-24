#!/usr/bin/env python3
"""Draws the item textures (16x16) of every wand and core and writes them into each workspace.

  python3 tools/assets/textures.py            # write the textures
  python3 tools/assets/textures.py --preview out.png

Wands are drawn like vanilla tools (handle bottom-left, head top-right): an oak handle, the redstone binding of the
2.0 recipe, and a gem head in the tier's colours. Cores are framed orbs with a symbol for their mode.
"""
from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
WORKSPACES = ['1.20.1', '1.21.1', '26.2', '26.3']
TEXTURES = 'common/src/main/resources/assets/reconstructedwands/textures/item'


def rgb(hex_color):
    hex_color = hex_color.lstrip('#')
    return tuple(int(hex_color[i:i + 2], 16) for i in (0, 2, 4)) + (255,)


class Canvas:
    def __init__(self):
        self.img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))

    def px(self, x, y, color):
        if 0 <= x < 16 and 0 <= y < 16:
            self.img.putpixel((x, y), rgb(color) if isinstance(color, str) else color)

    def get(self, x, y):
        return self.img.getpixel((x, y))

    def outline(self, color):
        """A 1px outline around every opaque pixel (on transparent neighbours only)."""
        src = self.img.copy()
        for y in range(16):
            for x in range(16):
                if src.getpixel((x, y))[3] == 0:
                    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        nx, ny = x + dx, y + dy
                        if 0 <= nx < 16 and 0 <= ny < 16 and src.getpixel((nx, ny))[3] > 0:
                            self.px(x, y, color)
                            break


# Wands -------------------------------------------------------------------------------------------------------------

HANDLE = ['#3c2a14', '#6b4a24', '#8d6436']  # dark, mid, light oak
BINDING = ['#5a0d0d', '#a8160f', '#e0362a']  # redstone binding

TIERS = {
    # shadow, base, light, shine
    'stone': ['#3f3f3f', '#6d6d6d', '#9a9a9a', '#c6c6c6'],
    'iron': ['#5e6266', '#a9afb5', '#dde2e6', '#ffffff'],
    'diamond': ['#0e5e5a', '#20c3b8', '#7df3e6', '#e8fffb'],
    'netherite': ['#2a2226', '#4d4047', '#76666e', '#a8969f'],
    'infinity': ['#3b0f6e', '#8a2be2', '#d08cff', '#fff3c4'],
}


def wand(tier):
    c = Canvas()
    shadow, base, light, shine = TIERS[tier]
    # Handle: a 2px diagonal from (1,14) up to (8,7).
    for i in range(8):
        x, y = 1 + i, 14 - i
        c.px(x, y, HANDLE[1])
        c.px(x + 1, y, HANDLE[2] if i % 3 else HANDLE[1])
        c.px(x, y - 1, HANDLE[0]) if i < 7 else None
    # Redstone binding around the middle of the handle.
    for (x, y), col in {(5, 10): 2, (6, 10): 1, (5, 9): 1, (6, 9): 0, (4, 11): 1, (7, 9): 0}.items():
        c.px(x, y, BINDING[col])
    # Head: a faceted gem, a 6x6 diamond shape centred on (11,4).
    head = {
        (11, 1): 3, (10, 2): 2, (11, 2): 2, (12, 2): 1,
        (9, 3): 2, (10, 3): 3, (11, 3): 2, (12, 3): 1, (13, 3): 1,
        (8, 4): 1, (9, 4): 2, (10, 4): 2, (11, 4): 1, (12, 4): 1, (13, 4): 0, (14, 4): 0,
        (9, 5): 1, (10, 5): 1, (11, 5): 1, (12, 5): 0, (13, 5): 0,
        (10, 6): 0, (11, 6): 0, (12, 6): 0,
        (11, 7): 0,
    }
    palette = [shadow, base, light, shine]
    for (x, y), col in head.items():
        c.px(x, y, palette[col])
    # Collar between handle and head.
    c.px(9, 6, '#d7a43a')
    c.px(10, 7, '#a77a22')
    c.px(9, 7, '#7a5616')
    if tier == 'infinity':
        for x, y in ((14, 1), (15, 2), (7, 2), (14, 7)):
            c.px(x, y, '#fff3c4')
    c.outline('#120d08')
    return c.img


# Cores -------------------------------------------------------------------------------------------------------------

FRAME = ['#4a3410', '#a8791f', '#f1c445']  # gold frame: dark, mid, light

CORES = {
    # orb shadow, base, light, symbol, symbol shadow
    'angel': ['#a36a00', '#f5c242', '#fff2b0', '#ffffff', '#d9c38a'],
    'destruction': ['#5c0b0b', '#c4241c', '#ff7a5c', '#1d0505', '#7d1410'],
    'exchange': ['#0a5131', '#22b46a', '#8ff0b7', '#ffffff', '#0b3d25'],
    'architect': ['#2b1a6b', '#5b47d6', '#a99bff', '#fff6d0', '#1d1450'],
}

SYMBOLS = {
    # 6x6 symbol drawn at (5..10, 5..10); 1 = symbol, 2 = symbol shadow
    'angel': [  # a feather
        '....11',
        '...111',
        '..111.',
        '.111..',
        '.22...',
        '2.....',
    ],
    'destruction': [  # a crack
        '...1..',
        '..11..',
        '..1...',
        '.111..',
        '...1..',
        '..1...',
    ],
    'exchange': [  # two arrows, one each way
        '....1.',
        '111111',
        '....1.',
        '.1....',
        '111111',
        '.1....',
    ],
    'architect': [  # a set square
        '1.....',
        '11....',
        '1.1...',
        '1..1..',
        '1...1.',
        '111111',
    ],
}


def core(mode):
    c = Canvas()
    shadow, base, light, symbol, symbol_shadow = CORES[mode]
    # Orb: a 10x10 disc centred on (7.5, 7.5).
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            d = (dx * dx + dy * dy) ** 0.5
            if d <= 5.2:
                shade = light if (dx + dy) < -3.5 else base if (dx + dy) < 2.5 else shadow
                c.px(x, y, shade)
            elif d <= 6.4:
                ring = FRAME[2] if (dx + dy) < -2 else FRAME[1] if (dx + dy) < 3 else FRAME[0]
                c.px(x, y, ring)
    # Gold studs at the four points of the frame.
    for x, y in ((7, 0), (8, 0), (0, 7), (0, 8), (15, 7), (15, 8), (7, 15), (8, 15)):
        c.px(x, y, FRAME[2] if x < 8 and y < 8 else FRAME[1])
    for row, line in enumerate(SYMBOLS[mode]):
        for col, ch in enumerate(line):
            if ch == '1':
                c.px(5 + col, 5 + row, symbol)
            elif ch == '2':
                c.px(5 + col, 5 + row, symbol_shadow)
    c.px(5, 4, '#ffffff') if mode != 'destruction' else None
    c.outline('#1a1206')
    return c.img


def all_textures():
    out = {f'{tier}_wand': wand(tier) for tier in TIERS}
    out.update({f'core_{mode}': core(mode) for mode in CORES})
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--preview')
    args = ap.parse_args()
    textures = all_textures()
    if args.preview:
        scale = 10
        sheet = Image.new('RGBA', (len(textures) * (16 * scale + 10) + 10, 16 * scale + 20), (52, 56, 62, 255))
        for i, img in enumerate(textures.values()):
            big = img.resize((16 * scale, 16 * scale), Image.NEAREST)
            sheet.paste(big, (10 + i * (16 * scale + 10), 10), big)
        sheet.save(args.preview)
        return
    for workspace in WORKSPACES:
        target = ROOT / workspace / TEXTURES
        if not target.parent.parent.exists():
            continue
        target.mkdir(parents=True, exist_ok=True)
        for name, img in textures.items():
            img.save(target / f'{name}.png')
        print(f'wrote {len(textures)} textures to {target.relative_to(ROOT)}')


if __name__ == '__main__':
    main()

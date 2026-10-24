# Patch notes

## 2.0.0 — The Architect Update

A full rebuild: the new Exchange and Architect cores, undo and redo (Ctrl+Z / Ctrl+Y), blocks from shulker boxes,
bundles, the Ender Chest and a bound chest, Ctrl + scroll size and Sneak + scroll lock, a HUD, a mode wheel and a new
settings screen, colour-coded previews, claim and protection checks on every loader, a synced server config with
deny tags and `/wands reload`, new textures and recipes, and repairable, enchantable wands. Available for Minecraft
1.20.1 (Fabric, Forge), 1.21.1, 26.2 and 26.3 (Fabric, NeoForge). 1.x wands keep their cores and settings.

Full notes: [patchnotes/2.0.0.md](patchnotes/2.0.0.md).

## 1.1.5

- Fixed NeoForge/Fabric 26.2 wand GUI crash (`Can only blur once per frame`, #1)
- Fixed survival undo duplication for placement and destruction
- Added `EnableUndo` client config (default `true`)

## 1.1.4

- Fixed 1.21.1 recipes failing to load (incorrect ingredient JSON format)

## 1.1.3

- Fixed core recipes on 1.21.1 / 26.2 to use vanilla ingredients
- Stone wand accepts `#minecraft:stone_crafting_materials` on 1.21.1 / 26.2

## 1.1.2

- Fixed missing textures on Minecraft 26.2 by adding `assets/reconstructedwands/items/` client item definitions

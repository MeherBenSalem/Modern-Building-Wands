# REConstructed Wands v1.1.4

### Bug Fixes
* **1.21.1 recipes actually load again** (Fabric + NeoForge). Ingredients were using the Minecraft **1.21.2+** string/`#tag` JSON format, so every shaped recipe failed to parse on 1.21.1 and craft grids showed nothing. Recipes now use the 1.21.1 `{ "item" }` / `{ "tag" }` format.
* Stone wand still accepts `#minecraft:stone_crafting_materials` (cobble, stone, etc.) via the correct tag object form
* Angel / Destruction cores keep vanilla ingredients (no `#c:` convention tags)

### Compatibility
* Drop-in replacement for 1.1.2 / 1.1.3 on all six loader targets
* Minecraft 1.20.1 — Fabric, Forge (version sync only)
* Minecraft 1.21.1 — Fabric, NeoForge (**recipe fix**)
* Minecraft 26.2 — Fabric, NeoForge (version sync only; already used the newer recipe format)

### Upgrade Notes
1. Install **1.1.4** for your loader from Modrinth / CurseForge / `releases/`
2. Fully restart the game (not only `/reload`)
3. Craft stone wand: sticks on the diagonal + any stone crafting material in the top-right
4. Iron / Diamond / Netherite / Infinity: same stick pattern with the upgrade material in the top-right

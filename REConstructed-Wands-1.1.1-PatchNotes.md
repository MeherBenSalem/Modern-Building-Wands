# REConstructed Wands v1.1.1

### Bug Fixes

* Fixed 1.20.1 crafting recipes not loading — recipes now use the correct `recipes/` folder and 1.20.1 JSON format (`result.item`, object-style ingredients).
* Fixed core recipes on 1.20.1 Forge by replacing `#c:` tag ingredients with vanilla items (feather, gold ingot, glass pane / diamond block, diamond pickaxe, glass pane).
* Fixed Forge and NeoForge wand limits — all wand tiers were incorrectly falling back to stone-tier placement limits (32 blocks) because config keys did not match item registry paths.
* Fixed Fabric stone wand angel and destruction cores doing nothing — limits were previously set to 0.
* Fixed wand upgrade recipe type key mismatch between upgrade crafting and config checks.

### Improvements

* Unified wand tier stats across Fabric, Forge, and NeoForge on all supported Minecraft versions.
* Aligned max placement range to 256 blocks on every loader.
* Added netherite wand tier limits on Forge/NeoForge (previously missing).

### Wand Tier Limits (all loaders)

| Wand | Construction | Angel | Destruction |
|------|-------------:|------:|------------:|
| Stone | 27 | 2 | 4 |
| Iron | 128 | 4 | 9 |
| Diamond | 1024 | 16 | 81 |
| Netherite | 2048 | 32 | 128 |
| Infinity | 2048 | 64 | 128 |

### Compatibility

* Drop-in replacement for v1.1.0 on all six loader targets (1.20.1 Fabric/Forge, 1.21.1 Fabric/NeoForge, 26.2 Fabric/NeoForge).
* No config migration required.

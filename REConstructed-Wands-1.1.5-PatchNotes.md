# REConstructed Wands v1.1.5

### Bug Fixes
* **26.2 NeoForge/Fabric wand GUI crash** — Removed duplicate background blur in `ScreenWand.extractRenderState` that caused `IllegalStateException: Can only blur once per frame` (#1).
* **Survival undo duplication (placement)** — Undoing a placement now removes the block without drops (`removeBlock(pos, false)`), so items are not duplicated when `UndoHistory` refunds the block item.
* **Survival undo duplication (destruction)** — Restoring a destroyed block in survival now consumes one matching block item from the player inventory before placing; creative mode skips consumption.

### New Features / Improvements
* **EnableUndo client config** — New `EnableUndo` option (default `true`) in client config disables OPTKEY undo (green highlight + sneak+RMB revert) when set to `false`. Server-side undo hooks are gated for future dedicated-server config support.

### Compatibility
* Drop-in replacement for 1.1.4 on all six loader targets
* Minecraft 1.20.1 — Fabric, Forge
* Minecraft 1.21.1 — Fabric, NeoForge
* Minecraft 26.2 — Fabric, NeoForge

### Upgrade Notes
1. Install **1.1.5** for your loader from Modrinth / CurseForge / `releases/`
2. Fully restart the game (not only `/reload`)
3. To disable undo: set `EnableUndo = false` under the `keys` section in `reconstructedwands-client.toml` (Forge/NeoForge) or equivalent client config

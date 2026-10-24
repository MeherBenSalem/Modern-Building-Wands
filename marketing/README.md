# Marketing

Listing copy and artwork for CurseForge and Modrinth.

| Path | What it is |
|------|------------|
| `curseforge/description.md` | The project description (Markdown; also works on Modrinth). Images load from this repo's `main` branch. |
| `curseforge/summary.txt` | The one-line summary (under 250 characters). |
| `images/banner.png` | 1800×600 header banner. |
| `images/icon.png` | 512×512 project icon. |
| `images/card-*.png` | 1600×900 feature cards used in the description and as gallery images. |
| `images/header-*.png`, `images/divider.png` | Transparent section headers and a divider for the description. |
| `images/screenshots/*.jpg` | Real in-game captures (Minecraft 26.3, Fabric) used by the cards and the gallery. |
| `source/render.cjs` | Renders every PNG above from the screenshots, the mod's own textures and the bundled fonts. |
| `source/backdrop.jpg` | Blurred background for the cards (a downscaled `build.jpg`). |
| `source/fonts/` | Lilita One and Nunito, both under the SIL Open Font License (licence files included). |

## Regenerating the images

Needs Node.js and Playwright with Chromium:

```bash
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs             # everything
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs card-wands  # one asset
```

The release name and the supported Minecraft versions and loaders are at the top of `render.cjs`. Supporting a new
Minecraft version means adding one line there, plus one row to the table in `description.md`. Wand and core icons are
read from `26.3/common/src/main/resources/assets/reconstructedwands/textures/item/`, so regenerated textures (see
`tools/assets/textures.py`) show up in the next render.

## Retaking the screenshots

`tools/qa/marketing_shots.py` builds each scene with server commands and captures it from a dev client (see
`tools/qa/` and `CONTRIBUTING.md`): a 1920×1080 window under Xvfb at GUI scale 3, on a 26.3 Fabric dev server whose
`server.properties` uses `level-name=showcase`, `level-type=minecraft:normal`, the seed given at the top of the script,
`broadcast-rcon-to-ops=false` and `view-distance=12` (so command output stays out of the chat and the horizon is
filled). Every screenshot is a single unedited frame.

| Screenshot | Scene |
|------------|-------|
| `build.jpg` | Construction: the next layer of a cottage wall, previewed, with the HUD |
| `angel.jpg` | Angel Core: a bridge growing over water, one block at a time |
| `destruction.jpg` | Destruction Core: a cliff face about to be cleared |
| `exchange.jpg` | Exchange Core: a cobblestone wall about to become stone bricks |
| `architect.jpg` | Architect Core: a ring previewed between two corners |
| `undo.jpg` | Holding Ctrl and sneaking: the blocks an undo would take back |
| `supply.jpg` | A wand bound to a chest, with shulker boxes in the hotbar and the HUD count |
| `screen.jpg`, `wheel.jpg` | The settings screen and the mode wheel |

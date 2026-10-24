# Contributing

Thanks for helping improve RE:Constructed Wands.

## How to contribute

1. Fork the repository and create a feature branch from `main`.
2. Make focused changes in the relevant Minecraft version workspace (`1.20.1/`, `1.21.1/`, `26.2/` or `26.3/`).
   A gameplay change usually belongs in all four: the `common/` code is nearly identical between them, and only the
   version-specific APIs differ.
3. Build the workspace you changed (`./gradlew build` from that folder, with Java 17 for 1.20.1, 21 for 1.21.1 and
   25 for 26.x). This also runs the unit tests.
4. Open a pull request describing the change and how you tested it.

## Layout

Each workspace is a MultiLoader Gradle build:

- `common/`: the game logic (`wand/`: planning, placing, supply, undo), items, config, network payloads, the
  command, and the client code (preview, HUD, screens, input). Nearly all changes happen here.
- `fabric/` and `neoforge/` (`forge/` on 1.20.1): entry points that register content and payloads and connect events,
  render hooks and protection checks to the common code.

Version-specific code is kept in a few places: `WandData` (how settings are stored), `supply/ContainerItems`
(shulker box and bundle contents), the GUI and render calls in `client/`, and the loader modules.

## Textures

The wand and core textures are drawn by `tools/assets/textures.py`, not edited by hand:

```bash
pip install pillow
python3 tools/assets/textures.py                     # writes the textures into every workspace
python3 tools/assets/textures.py --preview out.png   # a contact sheet
```

## Testing in game

`tools/qa/` drives dev servers over RCON and a real client under Xvfb (software rendering):

```bash
tools/qa/devrun.sh start-server 26.3 fabric
tools/qa/devrun.sh start-client 26.3 fabric                  # needs Xvfb on :1
python3 tools/qa/rcon.py "op Builder"
python3 tools/qa/wand_checks.py --mc 26.3 --out /tmp/wands-qa/shots   # --mc: the workspace's version
tools/qa/devrun.sh stop-client; tools/qa/devrun.sh stop-server 26.3
```

The dev server's `server.properties` needs `enable-rcon=true`, `rcon.password=qa`, `rcon.port=25575`,
`online-mode=false`, `level-type=minecraft:flat` and `white-list=false`. The 1.20.1 Forge dev server also needs
`advertiseDedicatedServerToLan = false` in `forge/runs/server/defaultconfigs/forge-server.toml` (in the dev
environment Forge's LAN advertiser loads a client-only class and the server stops). `wand_checks.py` uses every mode in
survival and checks undo, redo, size, lock, every block source, drops, the deny tag and `/wands reload`, and saves
screenshots of the HUD, the settings screen and the mode wheel. If you change gameplay, run it on every workspace
you touched, for Fabric and NeoForge/Forge.

## Coding expectations

- Prefer small, reviewable PRs over large mixed changes.
- Match existing naming, package layout, and MultiLoader patterns.
- Nothing may be lost or duplicated: take items before placing, refund on failure, and keep undo exact.
- Ask the loader's protection hooks (through `Platform`) before changing any block.
- Do not commit build output, IDE metadata, or local secrets.
- When fixing a bug, say which version/loaders you verified.

## Issues

Use GitHub Issues for bugs and feature requests. Include Minecraft version, mod loader, mod version, and steps to
reproduce when reporting bugs.

## Licensing

By contributing, you agree that your contributions are licensed under the Apache License, Version 2.0, unless you
state otherwise in the pull request.

# RE:Constructed Wands

Building wands for Minecraft: place or remove multiple blocks at once, with
optional cores, undo, and a configuration GUI.

| Workspace | Minecraft | Loaders | Java |
| --- | --- | --- | --- |
| `1.20.1` | 1.20.1 | Fabric + Forge | 17 |
| `1.21.1` | 1.21.1 | Fabric + NeoForge | 21 |
| `26.2` | 26.2 | Fabric + NeoForge | 25 |

## Features

- Construction and destruction modes
- Wand tiers from stone through infinity
- Core upgrades (angel / destruction)
- Undo for recent placements (can be disabled in client config)
- Fabric, Forge, and NeoForge via MultiLoader workspaces

## Installation

1. Download the jar for your Minecraft version and loader from
   [Modrinth](https://modrinth.com/mod/reconstructed-wands) or
   [CurseForge](https://www.curseforge.com/minecraft/mc-mods/re-constructed-wands).
2. Place it in your `mods` folder.
3. Restart the game.

## Usage

- **Right-click** — place or destroy according to current mode
- **Shift + right-click** — open the wand GUI

## Building

```powershell
cd 1.20.1   # or 1.21.1 / 26.2
.\gradlew.bat build
```

Use the JDK matching the workspace table. Output jars are under each loader
module's `build/libs/`.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
Security reports: [.github/SECURITY.md](.github/SECURITY.md).

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) and
[NOTICE](NOTICE).

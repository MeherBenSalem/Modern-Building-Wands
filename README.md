# REConstructed Wands

REConstructed Wands is maintained as three independent Minecraft MultiLoader workspaces. Gameplay code lives in each workspace's `common` module, with loader integration isolated in the loader modules.

| Workspace | Minecraft | Loaders | Java |
| --- | --- | --- | --- |
| `1.20.1` | 1.20.1 | Fabric + Forge | 17 |
| `1.21.1` | 1.21.1 | Fabric + NeoForge | 21 |
| `26.2` | 26.2 | Fabric + NeoForge | 25 |

## Building

Run the wrapper from the workspace you want to build:

```powershell
cd 1.20.1
.\gradlew.bat build
```

Use the matching JDK listed above. The consolidated `releases/` folder contains the final `1.1.0` runtime jars for all six loader targets.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).

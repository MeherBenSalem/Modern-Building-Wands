---
name: reconstructed-wands-release
description: Automates the full release workflow for REConstructed Wands (Modern-Building-Wands): semver bump across all MultiLoader workspaces, patch notes, build all six loader jars into releases/, commit, and push. Use when releasing, versioning, building all jars, creating patch notes, or when the user says "release", "bump version", or "build and push" for this mod.
---

# REConstructed Wands Release Workflow

Automates releases for the three independent MultiLoader workspaces in this repo.

| Workspace | Minecraft | Loaders | Gradle JVM |
| --- | --- | --- | --- |
| `1.20.1` | 1.20.1 | Fabric + Forge | JDK 17 |
| `1.21.1` | 1.21.1 | Fabric + NeoForge | JDK 21 |
| `26.2` | 26.2 | Fabric + NeoForge | JDK 25+ (system default) |

## Workflow Steps

Execute **in order**.

### Step 1: Determine Version Bump

Read current version from any `gradle.properties` (`version=X.Y.Z`). All three workspaces must stay in sync.

| Change Type | Bump |
| --- | --- |
| Bug fix, recipe/config fix | **patch** |
| New feature, enhancement | **minor** |
| Breaking change | **major** |

Default to **patch** when unsure.

### Step 2: Update Version

Edit `version=` in all three files:

- `1.20.1/gradle.properties`
- `1.21.1/gradle.properties`
- `26.2/gradle.properties`

Update `README.md` if it references the release version in `releases/`.

### Step 3: Create Patch Notes

Create or append `REConstructed-Wands-X.Y.Z-PatchNotes.md` in the repo root.

```markdown
# REConstructed Wands vX.Y.Z

### Bug Fixes
* ...

### Improvements
* ...

### Compatibility
* Drop-in replacement for previous version on all six loader targets.
```

Use `*` bullets. Keep entries user-facing.

### Step 4: Build All Jars

**JDK requirement:** 1.20.1/1.21.1 builds fail on Java 26 because `buildSrc` Groovy cannot parse class file major version 70. Use Temurin 17 and 21.

Preferred — run the release script from repo root:

```powershell
.cursor/skills/reconstructed-wands-release/scripts/release.ps1 -Version X.Y.Z
```

From anywhere, pass the repo path:

```powershell
~/.cursor/skills/reconstructed-wands-release/scripts/release.ps1 -Version X.Y.Z -RepoRoot "C:\path\to\Modern-Building-Wands"
```

Manual build per workspace:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
cd 1.20.1; .\gradlew.bat build --no-daemon

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
cd ..\1.21.1; .\gradlew.bat build --no-daemon

cd ..\26.2; .\gradlew.bat build --no-daemon
```

If Temurin is missing:

```powershell
winget install EclipseAdoptium.Temurin.17.JDK --accept-package-agreements --accept-source-agreements
winget install EclipseAdoptium.Temurin.21.JDK --accept-package-agreements --accept-source-agreements
```

### Step 5: Collect Jars into `releases/`

Copy only runtime jars (exclude `-sources` and `-javadoc`) matching the new version:

| Output jar |
| --- |
| `reconstructedwands-fabric-1.20.1-X.Y.Z.jar` |
| `reconstructedwands-forge-1.20.1-X.Y.Z.jar` |
| `reconstructedwands-fabric-1.21.1-X.Y.Z.jar` |
| `reconstructedwands-neoforge-1.21.1-X.Y.Z.jar` |
| `reconstructedwands-fabric-26.2-X.Y.Z.jar` |
| `reconstructedwands-neoforge-26.2-X.Y.Z.jar` |

Source paths: `{workspace}/{loader}/build/libs/`. The `releases/` folder is tracked in git (`!releases/*.jar` in `.gitignore`).

### Step 6: Commit

Stage source changes, patch notes, `README.md`, and `releases/*.jar`. Do not `git add .` blindly.

```
Update to vX.Y.Z
```

### Step 7: Push

```powershell
git push origin main
```

If not on `main`, confirm with the user first. Never force push.

## Important Notes

- All three `gradle.properties` versions must match exactly.
- Patch notes filename must match the version.
- Stop and fix build errors before committing.
- Jar naming comes from `multiloader-common.gradle`: `${mod_id}-${project.name}-${minecraft_version}-${version}.jar`.
- After code changes, run `graphify update .` if `graphify-out/graph.json` exists.

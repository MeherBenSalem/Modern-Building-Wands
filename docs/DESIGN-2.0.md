# RE:Constructed Wands 2.0: The Architect Update

Design notes for the 2.0 rebuild. Targets: Minecraft 1.20.1 (Fabric, Forge), 1.21.1, 26.2 and 26.3 (Fabric,
NeoForge). One feature set on every target; the common module holds the game logic, the client code and the config,
and each loader module only connects events.

## Goals

1. **Nothing is lost or duplicated.** Items are taken before a block is placed and handed back if the placement fails
   or is refused. Containers are edited through their item data, never through stale copies. Every action can be
   undone, and undo never prints items.
2. **Protection is asked before anything changes.** World bounds, `mayInteract`, adventure mode, the 3D range limit,
   the deny tags, and the loader's claim hooks: NeoForge/Forge place and break events (snapshot before placing,
   restored if cancelled), and on Fabric the break events plus the claim API that claim mods implement.
3. **The same mod on every loader.** 1.x had no config on either loader (all values were hard-coded), its Fabric build
   lacked scroll and mode controls, and destruction dropped items on NeoForge but deleted them on Fabric.
4. **Fill the gaps players ask for** (see "Market" below) without adding a dependency.

## Market

| Expectation | 1.1.5 | 2.0 |
| --- | --- | --- |
| Containers as a block source | advertised, but no handler was registered | shulker boxes, bundles, the Ender Chest and a bound chest |
| Claim/protection mods respected | NeoForge only, and the place event was fired after placing against the wrong block | every position checked before items move, on every loader |
| Server config | none (hard-coded) | `config/reconstructedwands/server.json`, synced to clients, `/wands reload` |
| Exchange (swap) mode | no | Exchange Core: swaps a surface, keeps orientation, returns the old blocks |
| Shapes | no | Architect Core: line, wall, floor, box and cube between two corners; circle and ring around a centre |
| Size control | fixed per tier | Ctrl + scroll sets the size from 1 to the tier's limit |
| Undo | last 3, hold Ctrl and sneak-click | configurable depth (10), undo and redo with Ctrl+Z / Ctrl+Y, the old gesture still works |
| HUD | no | mode, block count and limit, the block and how many you have, or what is wrong |
| Mode switching | GUI only on Fabric | a mode wheel (hold R), the options screen (G or sneak + right-click), scroll gestures on both loaders |
| Preview | outline, red only | outlines coloured by state: placing, missing blocks, undo |
| Random palette | uniform | weighted by how many hotbar slots hold each block |
| Durability | plain | repairable in an anvil, Unbreaking and Mending, Netherite by smithing upgrade |
| Recipes | the diagonal shape clashes with the vanilla spear | a redstone binding, so no clash on any version |
| License hygiene | ThetaDev credited by name only | the MIT notice of Construction Wand ships in the jars |

Out of scope for 2.0: copy/paste templates, mirror/array, AE2/RS networks, energy costs. They carry the highest dupe
and block-entity risk in the competitors and need optional dependencies per loader.

## Content (IDs are unchanged from 1.x)

| Item | ID | Notes |
| --- | --- | --- |
| Stone, Iron, Diamond, Netherite, Infinity Wand | `stone_wand` ... `infinity_wand` | Infinity is unbreakable; the others repair with their material |
| Angel Core | `core_angel` | place in mid-air or behind the block you face |
| Destruction Core | `core_destruction` | remove a surface |
| Exchange Core (new) | `core_exchange` | swap a surface for another block |
| Architect Core (new) | `core_architect` | build shapes with two clicks |

Cores are installed by crafting a wand with a core (the stone wand takes none). Settings live in the wand's custom
data under `wand_options`, with the same keys as 1.x (`cores`, `cores_sel`, `lock`, `direction`, `replace`, `match`,
`random`), so existing wands keep their cores and settings. New keys: `size`, `shape`, `anchor`, `bound`.

### Default tiers (server.json)

| Tier | Durability | Construction | Angel reach | Destruction / Exchange | Cores |
| --- | --- | --- | --- | --- | --- |
| Stone | 131 | 27 | 2 | 4 | no |
| Iron | 250 | 128 | 4 | 9 | yes |
| Diamond | 1561 | 1024 | 16 | 81 | yes |
| Netherite | 2031 | 2048 | 32 | 128 | yes |
| Infinity | unbreakable | 2048 (8192 in creative) | 64 | 128 | yes |

The Architect limit is the Construction limit.

## Modes

- **Construction**: extend the clicked face across the matching blocks behind it (the 1.x algorithm), limited by
  size, lock and match.
- **Angel**: place one block behind the clicked block or in front of you in mid-air.
- **Destruction**: remove the exposed surface of matching blocks. Drops are the block's normal drops as if broken by
  the tool in your offhand (empty hand otherwise) and go straight to your inventory; `destruction_drops: false` deletes
  them instead. Blocks with block entities and unbreakable blocks are skipped in survival.
- **Exchange**: replace the exposed surface of matching blocks with the replacement block (offhand, else the wand's
  palette). Orientation (facing, axis, half, slab type, stair shape) carries over. The old blocks come back as items.
- **Architect**: right-click sets the first corner, the preview follows your aim, the second right-click builds the
  shape. Lines, walls, floors, boxes and cubes fill the space between the two; circles and rings are centred on the
  first click with the second on their edge. Right-click the air or switch modes to clear the corner.

## Supply order

The replacement block (offhand) or the target block and its similar blocks, then: loose stacks in the main inventory,
shulker boxes and bundles in the inventory, the bound container, the Ender Chest, and finally the hotbar and offhand.
Sneak + right-click a container (chest, barrel, placed shulker box, any modded block that exposes a vanilla
`Container`) binds the wand to it; doing it again unbinds. Bound containers must be loaded, in the same dimension and
within `bound_range` blocks.

## Controls

| Input | Action |
| --- | --- |
| Right-click a block | use the active mode |
| Right-click the air | Angel: place in mid-air. Architect: clear the corner |
| Sneak + right-click the air, or G | options screen |
| Hold R | mode wheel: release over a mode to pick it |
| Ctrl + scroll | size (1 to the tier's limit) |
| Sneak + scroll | direction lock (Construction, Destruction, Exchange) or shape (Architect) |
| Ctrl + Z / Ctrl + Y | undo / redo (also hold Ctrl and sneak + right-click to undo) |
| Sneak + right-click a container | bind or unbind it as a block source |

Every key is rebindable or can be turned off in `client.json`.

## Config

`config/reconstructedwands/server.json` (tiers, cores on/off, creative limit, 3D max range, undo depth, block sources,
destruction drops, similar-block groups, angel while falling) is synced to clients so the preview matches what the
server will do. `client.json` holds the preview colours, the HUD and the input options. `/wands reload` (permission
level 2) reloads both and resyncs players. Deny tags: `reconstructedwands:denied` (never placed, removed or exchanged
by a wand) and `reconstructedwands:no_dismantle` (never removed by Destruction or Exchange).

## Architecture

```
common/   ReconstructedWands (init), platform/ (Platform, Registrar), content/, item/, wand/ (job, actions, shapes,
          supply, undo, placement), config/, network/ (payloads), command/, client/ (HUD, screens, preview, input)
fabric/   entry points: registration, payload registration, events, render hooks
neoforge/ entry points: deferred registers, payload handlers, events, render hooks (forge/ on 1.20.1)
```

Unit tests cover the Minecraft-free parts: shapes, the weighted palette and the config files.

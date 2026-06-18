# SoupPvP — Paper / Leaf 1.21.8 Port

This is the **1.21.8 port** of [`SgtMatin/SoupPvP`](https://github.com/SgtMatin/SoupPvP),
a Lunar Network-style soup PvP gamemode plugin (kits, perks, killstreaks, scoreboard,
sumo events, coinflip wagers, bounty, shop, tiers, statistics).

The port targets the **Paper 1.21.8 API** and runs on any Paper-compatible server:
**Paper, Leaf, Purpur, Pufferfish, Folia\*** (Folia not officially tested).

---

## Quick start — drop-in install

1. Make sure your server is running **Java 21** and **Paper / Leaf 1.21.8**.
2. Drop `SoupPvP-1.21.8-port.jar` into your `plugins/` directory.
3. (Optional) Install softdeps for extra features:
   - **PlaceholderAPI** — `%souppvp_*%` placeholders
   - **DecentHolograms** — `/soupholo` command
   - **ViaVersion / ViaBackwards / ViaRewind** — legacy client support
4. Restart the server. Edit `plugins/SoupPvP/config.yml` (Mongo creds, spawn coords),
   then `/reload confirm` or restart.

The prebuilt jar lives in `target/SoupPvP-1.21.8-port.jar` (build below to regenerate).

---

## Building from source

```bash
# Requires Java 21 + Maven 3.8+
mvn package
```

The shaded jar will be at `target/SoupPvP-1.21.8-port.jar`. It bundles:
- `commons-lang3`
- `mongo-java-driver 3.12.x`
- `drink` command framework (unpacked from `lib/drink-1.0.5.jar`)

---

## What changed during the port

### Build / project
- Java 8 → **Java 21**
- Spigot 1.8.8 → **Paper API 1.21.8** (`io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT`)
- `plugin.yml`: added `api-version: '1.21'`, declared commands explicitly,
  modernised softdepends
- `pom.xml`: Java 21 source/target, fresh shade plugin (`3.5.3`), antrun unpacks Drink

### Material renames (1.8 → 1.21)
| Old | New |
|---|---|
| `MUSHROOM_SOUP` | `MUSHROOM_STEW` |
| `INK_SACK` | `INK_SAC` |
| `STAINED_GLASS` / `STAINED_GLASS_PANE` | `RED_STAINED_GLASS` / `RED_STAINED_GLASS_PANE` |
| `SKULL_ITEM` | `PLAYER_HEAD` |
| `CARPET` | `WHITE_CARPET` |
| `MONSTER_EGG` | `BAT_SPAWN_EGG` |
| `WORKBENCH` | `CRAFTING_TABLE` |
| `RAW_FISH` / `COOKED_FISH` | `COD` / `COOKED_COD` |
| `SIGN_POST` / `WALL_SIGN` / `SIGN` | `OAK_SIGN` / `OAK_WALL_SIGN` |
| `SNOW_BALL` | `SNOWBALL` |
| `WATCH` | `CLOCK` |
| `WEB` | `COBWEB` |
| `REDSTONE_TORCH_ON/OFF` | `REDSTONE_TORCH` |
| `WOOL` | `WHITE_WOOL` |
| `LEASH` | `LEAD` |
| `STATIONARY_WATER/LAVA` | `WATER` / `LAVA` |
| `BOOK_AND_QUILL` | `WRITABLE_BOOK` |
| `EXPLOSIVE_MINECART` | `TNT_MINECART` |
| `FIREWORK` / `FIREWORK_CHARGE` | `FIREWORK_ROCKET` / `FIREWORK_STAR` |
| `GOLD_<armor/sword/axe>` | `GOLDEN_<armor/sword/axe>` |
| `SPECKLED_MELON` | `GLISTERING_MELON_SLICE` |

### Enchantment renames
`DAMAGE_ALL → SHARPNESS`, `DAMAGE_UNDEAD → SMITE`, `DAMAGE_ARTHROPODS → BANE_OF_ARTHROPODS`,
`DURABILITY → UNBREAKING`, `PROTECTION_ENVIRONMENTAL → PROTECTION`, `PROTECTION_FALL → FEATHER_FALLING`,
`PROTECTION_FIRE → FIRE_PROTECTION`, `PROTECTION_EXPLOSIONS → BLAST_PROTECTION`,
`PROTECTION_PROJECTILE → PROJECTILE_PROTECTION`, `ARROW_DAMAGE → POWER`, `ARROW_KNOCKBACK → PUNCH`,
`ARROW_FIRE → FLAME`, `ARROW_INFINITE → INFINITY`, `LOOT_BONUS_MOBS → LOOTING`,
`LOOT_BONUS_BLOCKS → FORTUNE`, `DIG_SPEED → EFFICIENCY`, `OXYGEN → RESPIRATION`,
`WATER_WORKER → AQUA_AFFINITY`.

### PotionEffectType renames
`SLOW → SLOWNESS`, `SLOW_DIGGING → MINING_FATIGUE`, `FAST_DIGGING → HASTE`,
`INCREASE_DAMAGE → STRENGTH`, `HEAL → INSTANT_HEALTH`, `HARM → INSTANT_DAMAGE`,
`JUMP → JUMP_BOOST`, `CONFUSION → NAUSEA`, `DAMAGE_RESISTANCE → RESISTANCE`.

### Sound renames
Bulk-renamed legacy 1.8 sounds (e.g. `WITHER_SHOOT → ENTITY_WITHER_SHOOT`,
`BAT_TAKEOFF → ENTITY_BAT_TAKEOFF`, `ANVIL_LAND → BLOCK_ANVIL_LAND`,
`NOTE_PLING → BLOCK_NOTE_BLOCK_PLING`, etc.).

### Removed / stubbed dependencies
The following 1.8-only deps were dropped to make the port work:

| Removed | Reason | Behaviour now |
|---|---|---|
| `kyori.adventure` packetevents shading | unused | n/a |
| `com.lunarclient.bukkitapi` | EOL legacy BukkitAPI shim | **Replaced** in Phase 2 with `com.lunarclient:apollo-api` 1.2.6 — nametag overrides work again via `NametagModule.overrideNametag(...)` |
| `xyz.refinedev.phoenix:pxAPI` | dep moved repo | **Restored** in Phase 2 (`maven.refinedev.org`, version `1.7.5.1`) — rank-coloured kill / nametag messages back |
| `NoSequel:TabAPI` | unused | n/a |
| `com.github.retrooper:packetevents-spigot` | unused | n/a |
| `cn.dreeam.leaf:leaf-api` | only needed for NMS we removed | n/a |
| `eu.decentsoftware-eu:decentholograms` | hard-coded in old build | Optional softdep, accessed reflectively in `/soupholo` |

### NMS rewrites
| File | Original | Port |
|---|---|---|
| `util/particles/ParticleEffect.java` | 1.8 NMS reflection | Modern `World.spawnParticle()` mapping legacy names to `org.bukkit.Particle` |
| `util/particles/AdvancedParticleEffect.java` | 1.8 NMS reflection | Same — preserved public API |
| `util/projectile/projectile/ItemProjectile.java` | extended NMS `EntityItem` | Rewritten on top of Bukkit `Item` entity + per-tick `BukkitRunnable` collision check |
| `util/projectile/projectile/TNTProjectile.java` | extended NMS `EntityTNTPrimed` | **Phase 2** — rewritten on top of Bukkit `TNTPrimed` entity with custom velocity + per-tick collision check + `createExplosion(...)` on hit. Fires the original `CustomProjectileHitEvent` so call sites are unchanged. |
| `util/projectile/projectile/OrbProjectile/ProjectileScheduler` | NMS-extending | Stubs (these classes were dead code in the original) |
| `util/ReflectionUtil(s).java` | Walked `v1_8_R3` package | Stubs operating on plain Java reflection (no longer NMS-only) |
| `kit/inherit/VampireKit.java` | Used `MinecraftServer.currentTick` | Internal tick counter |
| `scoreboard/ScoreboardAdapter.java` | Cast to `CraftPlayer` for ping | Uses `Player.getPing()`. **Phase 2** — Apollo `RichPresenceModule` re-wired through `apollo-api 1.2.6`, lazily looked-up so the plugin still loads even if Apollo is not installed.
| `listener/LunarClientListener.java` | legacy `com.lunarclient.bukkitapi.LunarClientAPI` | **Phase 2** — rewritten on Apollo `NametagModule.overrideNametag(...)` using Adventure `Component` lines built from the same legacy coloured strings the original plugin produced. Phoenix `SharedAPI` is queried for the rank-coloured name with a safe `try/catch` fallback so the plugin still works without Phoenix installed. |
| `handlers/PacketBorderHandler.java` | `sendBlockChange(Material, byte)` | `sendBlockChange(Location, BlockData)` |

### Misc fixes
- `Bukkit.dropItem` / `EntityType.DROPPED_ITEM` → `EntityType.ITEM`
- `EntityType.SNOWMAN` → `SNOW_GOLEM`, `FISHING_HOOK` → `FISHING_BOBBER`
- `Effect.HUGE_EXPLOSION / EXPLOSION_HUGE` → `Effect.SMOKE` (closest equivalent
  – Bukkit `Effect` enum no longer exposes a vanilla "huge explosion" world-effect ID)
- All legacy `World.spigot().playEffect(...)` calls migrated to
  `World.spawnParticle(...)` calls using the modern `Particle` enum.
- `Block.getTypeId()` removed → switched to `Material` comparisons.
- `Inventory.getTitle()` removed → switched to `InventoryView.getTitle()`.
- `org.apache.commons.lang` (long EOL) → `org.apache.commons.lang3`.
- `WordUtils` now imported from `org.apache.commons.lang3.text.WordUtils`.
- `MongoDB` driver bumped to `3.12.14` (still classic, still works with the original
  `MongoClient` API used in the codebase).

---

## Known limitations of the port

The original codebase has very deep 1.8 NMS bindings. To get to a clean compile this
port replaces a handful of FX-heavy primitives with the closest modern equivalent:

1. **`OrbProjectile` + `ProjectileScheduler`** still kept as stubs — they were dead code
   in the original game logic, no in-game feature relies on them. Ask if you want them
   reimplemented.
2. **`ItemProjectile`** (used by `NinjaKit` shuriken + `TorchKit` dragon-breath) has
   been re-implemented on top of a regular dropped `Item` entity. Behaviour is
   equivalent but projectile physics may feel slightly different vs. the original NMS impl.
3. **DecentHolograms cycling leaderboards** — `/soupholo cycle` reports "not implemented
   in this port" until reintroduced. `/soupholo move` still works if DH is installed.

If any of these matter, just ask — see "Phase 3 backlog" in `PRD.md`.

### Optional 3rd-party softdepends used at runtime
- **Apollo (Lunar Client Bukkit)** for nametag overrides + Rich Presence. Get it from
  https://lunarclient.dev/apollo/downloads — drop the Bukkit Apollo jar in `plugins/`.
- **Phoenix (Refined Phoenix)** for rank-coloured kill / nametag prefixes
  (groupId `xyz.refinedev.phoenix`).
- **PlaceholderAPI** for `%souppvp_*%` placeholders.
- **DecentHolograms** for the `/soupholo` command.

The plugin will silently no-op those integrations if the respective plugin is missing.

---

## File map

```
SoupPvP/
├── pom.xml                          # Java 21 + Paper 1.21.8 + Drink
├── lib/
│   └── drink-1.0.5.jar              # Drink command framework (shaded at build)
├── src/main/java/kami/gg/souppvp/   # All plugin sources (≈300 files)
├── src/main/resources/
│   ├── plugin.yml                   # api-version: '1.21'
│   └── config.yml                   # Mongo + spawn + sumo config
└── target/SoupPvP-1.21.8-port.jar   # The drop-in jar (after `mvn package`)
```

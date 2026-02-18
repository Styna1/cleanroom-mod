# Changelog

<<<<<<< HEAD
## [0.1.3] - 2026-02-15

### Changed
- Added workspace `.vscode/settings.json` to force Cursor/VS Code Java + Gradle tooling to Java 25 (`jdk-25.0.2.10-hotspot`) and avoid `UnsupportedClassVersionError` from RetroFuturaGradle.

## [0.1.2] - 2026-02-15

### Fixed
- Updated playtime reward chat formatting so `(default)` is replaced with the configured main prefix (default `&7[&bPrivateServer&7]`), producing `[PrivateServer]` instead of showing `(default)`.

## [0.1.1] - 2026-02-14

### Changed
- Moved playtime reward settings into `modules/economy.json` so server owners can tune:
  - `playtimeRewardIntervalMinutes`
  - `playtimeRewardAmount`
  - `playtimeAfkTimeoutMinutes`
  - `playtimeRewardEnabled`
  - `playtimeRewardMessage`
- Playtime reward messages now use the main mod prefix from `config.json` (default `[PrivateServer]`) instead of the literal `(default)` text.

### Fixed
- Fixed time-is-money reward handling to read runtime values from config and send rewards/messages consistently.
- Fixed command activity tracking so command usage still counts as player activity even when combat tagging is disabled.

=======
>>>>>>> 293884e14fd113d3dc79e066dba0a1ad26810e84
## [0.1.0] - 2026-02-13

### Added
- Added `/pay <player> <amount>` player-to-player transfers for the economy module.
- Added active-playtime economy rewards: players now earn `$150` every 30 minutes of non-AFK playtime with the message `(default) you've received $<amount> for playing on the server!`.
- Added a high-memory safety chat warning that broadcasts `Server might crash, memory maxed.` when JVM memory usage becomes critical (with cooldown to prevent spam).
- Added countdown ding sounds for `/home` and `/back` teleports each second during warmup, plus an arrival ding on completion.

### Changed
- Opened all permission level `0` commands by default in command permission checks, while keeping admin-level commands restricted.

### Fixed
- Fixed `/tpa` and `/tpahere` acceptance routing so the correct player is teleported to the correct destination.
- Hardened cross-dimension teleport execution for `/home`, `/back`, and TPA flows by validating target dimensions before transfer.
- Fixed default access behavior for `/back`, `/bal`, and `/baltop` by applying consistent level-0 permission handling.

## [0.0.2] - 2026-02-11

### Changed
- Wired `build.gradle` to use `minecraft_version` from `gradle.properties` for centralized Minecraft target configuration.
- Updated teleport dimension transfer to use `changeDimension(int)` for compatibility with Forge variants that changed `ITeleporter`.
- Added cocaine consume command-based effect rolling: 4 weighted options, with a 1% chance to apply all configured effects.
- Added a custom `privateserver:dizzy` effect and configured cocaine defaults to include it.
- Moved cocaine to the Food creative tab so it appears in creative inventory and JEI/HEI item listings.
- Home command permissions are explicitly open to non-ops (`/home`, `/sethome`, `/delhome`, `/renamehome`).
- Added optional Chisel integration so sugar and cocaine are in the same chisel group (sugar can be chiseled into cocaine when Chisel is present).
- Added `/home` teleport cost support with a default of `$20` (`teleport.json -> homeCost`).

### Fixed
- Bundled SQLite JDBC in the mod jar by switching dependency scope to `embed`, preventing `No suitable driver found` in Prism/Cleanroom runtime.
- Explicitly loaded the SQLite driver before creating DB connections in economy, teleport, and discord link stores.
- Players with the dizzy effect now have a configurable 15% trip chance per second that drops their held item.

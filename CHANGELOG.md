# Changelog

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

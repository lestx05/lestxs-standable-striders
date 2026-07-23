# Changelog

All notable changes to Standable Striders are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and the project uses [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2026-07-23

### Added

- Official support for Minecraft Java Edition 26.2 on Fabric.
- GameTests for the 10-tick platform timeout, unsupported lava, baby striders,
  spectators, mounted players, vehicle collision, and angular leash momentum.
- Curated release notes generated from this changelog.

### Changed

- Migrated the project to Java 25, Fabric Loader 0.19.3, Fabric API
  0.155.2+26.2, Fabric Loom 1.17.17, and the unobfuscated Minecraft toolchain.
- Updated platform knockback handling for Minecraft 26.2's damage-aware
  knockback API.
- Uses Minecraft's own 90-degree angle helper and mirrors the Happy Ghast's
  same-vehicle collision and leash-momentum behavior.
- GitHub Releases now publish only from version tags and include the runtime
  JAR, SHA-256 checksum, and curated notes.

### Compatibility

- Requires Minecraft 26.2 and Java 25.
- Not compatible with Minecraft 1.21.11. Remove the 0.2.2 JAR before installing
  1.0.0; do not keep both versions in the `mods` directory.

## [0.2.2] - 2026-07-14

### Added

- Locked head pitch at 0 degrees while platformed.
- Prevented knockback, explosions, and external pushes from moving a
  platformed strider while preserving normal damage.

## [0.2.1] - 2026-07-14

### Added

- In-world GameTests for platform behavior, rotation, support removal, gravity,
  and damage.

## [0.2.0] - 2026-07-14

### Added

- Happy Ghast-style platform detection, position synchronization, 10-tick
  timeout, 60-tick load grace period, and 90-degree facing lock.
- Required a collidable supporting block beneath the strider while preserving
  gravity.

## [0.1.1] - 2026-07-14

### Fixed

- Removed an invalid `travel` injection that crashed Minecraft 1.21.11.

### Added

- Dedicated-server startup smoke testing.

## [0.1.0] - 2026-07-14

### Added

- Initial Fabric 1.21.11 release.
- Automated version-tagged GitHub Releases with a runtime JAR and SHA-256
  checksum.

[Unreleased]: https://github.com/lestx05/lestxs-standable-striders/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/lestx05/lestxs-standable-striders/compare/v0.2.2...v1.0.0
[0.2.2]: https://github.com/lestx05/lestxs-standable-striders/compare/v0.2.1...v0.2.2
[0.2.1]: https://github.com/lestx05/lestxs-standable-striders/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/lestx05/lestxs-standable-striders/compare/v0.1.1...v0.2.0
[0.1.1]: https://github.com/lestx05/lestxs-standable-striders/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/lestx05/lestxs-standable-striders/releases/tag/v0.1.0

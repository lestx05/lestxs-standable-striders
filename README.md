# Standable Striders

[![Build](https://github.com/lestx05/lestxs-standable-striders/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/lestx05/lestxs-standable-striders/actions/workflows/build.yml)
[![Latest release](https://img.shields.io/github/v/release/lestx05/lestxs-standable-striders?display_name=tag&sort=semver)](https://github.com/lestx05/lestxs-standable-striders/releases/latest)
[![License: MIT](https://img.shields.io/github/license/lestx05/lestxs-standable-striders)](LICENSE)

Standable Striders is a small, open-source Fabric mod for Minecraft Java
Edition 26.2. When a player stands on a supported adult strider, the strider
acts as a stable platform using the same core platform-state ideas as the
vanilla Happy Ghast.

The mod remains intentionally focused: it does not add mobs, items,
configuration files, commands, or world-generation content.

## Features

- Detects players standing on an adult strider using the Happy Ghast-style
  detection volume and root-vehicle check.
- Snaps the strider to the nearest 90-degree yaw and keeps its body and head
  facing straight ahead while platformed.
- Stops navigation, rider control, horizontal movement, damage knockback,
  external pushes, and angular leash momentum.
- Preserves vertical movement and gravity.
- Uses the vanilla 10-tick exit timeout and 60-tick post-load grace behavior.
- Requires a collidable supporting block directly below the strider. Lava,
  another entity, or empty space alone does not count as support.
- Ignores spectators, players riding the same strider, baby striders, and dead
  striders.

## Compatibility

| Mod version | Minecraft | Java | Status |
| --- | --- | --- | --- |
| 1.0.x | 26.2 | 25 or newer | Current |
| 0.2.2 | 1.21.11 | 21 or newer | Legacy |

Standable Striders is a Fabric mod and requires Fabric API. For multiplayer,
install it on the dedicated server and on every participating client. For
single-player, install it in the local client instance.

Do not keep multiple Standable Striders JARs in the same `mods` directory.
The Minecraft 26.2 build is not compatible with Minecraft 1.21.11.

## Installation

1. Install the Fabric Loader version required by your Minecraft release.
2. Install a compatible build of Fabric API.
3. Download the Standable Striders JAR that matches your Minecraft version.
4. Place the JAR in the instance or server `mods` directory.
5. In multiplayer, repeat the installation for the server and every client.

For the current Minecraft 26.2 release, use:

- Fabric Loader 0.19.3 or newer
- Fabric API 0.155.2+26.2 or a newer compatible 26.2 build
- Java 25 or newer

## Downloads

Maintainer-built releases are available from
[GitHub Releases](https://github.com/lestx05/lestxs-standable-striders/releases).
Download `standable-striders-<version>.jar`; files containing `-sources` are
for development and are not needed to play.

The existing
[Modrinth page](https://modrinth.com/mod/lestxs-standable-striders) is currently
a legacy Minecraft 1.21.11 listing; the Minecraft 26.2 build has not been
published there yet. That older artifact predates the current 26.2 codebase and
must not be installed alongside the current JAR.

Every GitHub Release includes `SHA256SUMS.txt` so the runtime JAR can be
verified after download.

## Reporting issues

Use the structured
[issue chooser](https://github.com/lestx05/lestxs-standable-striders/issues/new/choose)
for reproducible bugs, compatibility problems, feature requests, and support
questions.

Before reporting a problem:

1. Confirm that Minecraft, Fabric Loader, Fabric API, Java, and the mod version
   are compatible.
2. Remove duplicate or older Standable Striders JARs.
3. Reproduce the issue with only Standable Striders, Fabric API, and required
   dependencies when possible.
4. Keep `latest.log`, the crash report, and the exact mod list available.
5. Search existing issues for the same problem.

Do not disclose security vulnerabilities in a public issue. Follow the
[security policy](SECURITY.md) instead.

## Building from source

Clone the repository and run:

```bash
./gradlew build
```

On Windows PowerShell or Command Prompt:

```powershell
.\gradlew.bat build
```

The runtime JAR is generated in `build/libs/`. A full build also runs the
in-world GameTests. The dedicated-server smoke test can be run separately:

```bash
python3 scripts/smoke_test_server.py
```

Development requires Java 25. Import the Gradle project into IntelliJ IDEA or
another Java IDE and use the Loom-generated run configurations. Gameplay
behavior is implemented by `StriderMixin`; vanilla files are not replaced.

## Contributing and project policies

- [Contributing guide](CONTRIBUTING.md)
- [Support policy](SUPPORT.md)
- [Security policy](SECURITY.md)
- [Code of Conduct](CODE_OF_CONDUCT.md)
- [Changelog](CHANGELOG.md)
- [Release process](docs/RELEASING.md)

Standable Striders code and original documentation are distributed under the
[MIT License](LICENSE). The [Code of Conduct](CODE_OF_CONDUCT.md) contains
adapted Contributor Covenant text under the separate license stated in that
file. Contributions are licensed under the terms applicable to the file being
changed.

## Disclaimer

This is an unofficial Minecraft mod. It is not approved by or associated with
Mojang Studios, Microsoft, FabricMC, or Modrinth. Minecraft is a trademark of
Microsoft Corporation.

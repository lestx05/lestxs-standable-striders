# Standable Striders

A lightweight Fabric mod for Minecraft Java Edition 26.2. Adult striders
copy the vanilla Happy Ghast's platform-state behavior whenever a player stands
on top, provided a collidable block directly supports the strider.

## Current behavior

- Uses the Happy Ghast's exact player detection volume and root-vehicle point
  check, ignoring spectators and players riding striders.
- Synchronizes platform state between server and client, including the Happy
  Ghast's precise position update on entry and 10-tick exit grace period.
- Snaps to and locks the nearest multiple of 90 degrees based on the strider's
  facing direction when platform mode begins, while locking its head pitch at
  0 degrees so it keeps looking straight ahead.
- Stops navigation, rider control, horizontal movement, and horizontal drift
  while preserving vertical velocity and gravity.
- Clears angular leash momentum while platformed, matching the Happy Ghast's
  stop behavior without disabling gravity.
- Still takes damage normally, but rejects damage knockback, explosions, and
  other external push vectors while acting as a platform.
- Requires a collidable block directly below the strider. Lava, another entity,
  or empty space alone cannot support platform mode.
- Preserves the short platform timeout through world saves and uses the same
  60-tick post-load grace period as the Happy Ghast.
- Runs on both dedicated servers and integrated single-player servers.

## Requirements

- Minecraft Java Edition 26.2
- Java 25
- Fabric Loader 0.19.3 or newer
- Fabric API 0.155.2+26.2 or newer compatible 26.2 build

Version 1.0.0 is not compatible with Minecraft 1.21.11. Players upgrading from
0.2.2 must remove the old JAR before installing the 26.2 version; never keep
both in the `mods` directory.

## Download

Download the ready-to-use mod JAR from
[GitHub Releases](https://github.com/lestx05/lestxs-standable-striders/releases).
Use the file named `standable-striders-<version>.jar`; source and development
JARs are not required to play. Release notes and previous Minecraft-compatible
versions are recorded in the [changelog](CHANGELOG.md).

## Build

```bash
./gradlew build
```

On Windows PowerShell or Command Prompt, use `.\gradlew.bat build`.

The distributable mod JAR is generated in `build/libs/`. The file whose name
contains `-sources` is for development and is not required to play.

## Development

Import the Gradle project into IntelliJ IDEA or another Java IDE, then use the
Loom-generated client and server run configurations. The gameplay behavior is
implemented by `StriderMixin`; no vanilla files are replaced.

## Status

The project builds and runs automated in-world GameTests for platform entry,
90-degree yaw and zero-degree head-pitch locking, damage without knockback,
horizontal locking, angular leash momentum, the 10-tick grace period, support
removal, resumed falling, unsupported lava, babies, spectators, mounted
players, and vehicle collision. It also performs a dedicated-server bootstrap
test on every push and pull request. Version tags publish the runtime JAR,
curated changelog notes, and a SHA-256 checksum only after these checks pass.

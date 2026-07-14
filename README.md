# Standable Striders

A lightweight Fabric mod for Minecraft Java Edition 1.21.11. Adult striders
copy the vanilla Happy Ghast's platform-state behavior whenever a player stands
on top, provided a collidable block directly supports the strider.

## Current behavior

- Uses the Happy Ghast's exact player detection volume and root-vehicle point
  check, ignoring spectators and players riding striders.
- Synchronizes platform state between server and client, including the Happy
  Ghast's precise position update on entry and 10-tick exit grace period.
- Snaps to and locks the nearest multiple of 90 degrees based on the strider's
  facing direction when platform mode begins.
- Stops navigation, rider control, horizontal movement, and horizontal drift
  while preserving vertical velocity and gravity.
- Requires a collidable block directly below the strider. Lava, another entity,
  or empty space alone cannot support platform mode.
- Preserves the short platform timeout through world saves and uses the same
  60-tick post-load grace period as the Happy Ghast.
- Runs on both dedicated servers and integrated single-player servers.

## Requirements

- Minecraft Java Edition 1.21.11
- Java 21
- Fabric Loader 0.19.3 or newer
- Fabric API 0.141.4+1.21.11 or newer compatible 1.21.11 build

## Download

Download the ready-to-use mod JAR from
[GitHub Releases](https://github.com/lestx05/lestxs-standable-striders/releases).
Use the file named `standable-striders-<version>.jar`; source and development
JARs are not required to play.

## Build

```bash
./gradlew build
```

The remapped mod JAR is generated in `build/libs/`. Do not distribute the JAR
whose filename contains `-dev`.

## Development

Import the Gradle project into IntelliJ IDEA or another Java IDE, then use the
Loom-generated client and server run configurations. The gameplay behavior is
implemented by `StriderMixin`; no vanilla files are replaced.

## Status

The project builds and performs a dedicated-server bootstrap test automatically
on every push and pull request. Version changes publish the remapped JAR and its
SHA-256 checksum to GitHub Releases only after the same bootstrap test passes.

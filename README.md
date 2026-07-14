# Standable Striders

A lightweight Fabric mod for Minecraft Java Edition 1.21.11. Striders stop and
act as stable platforms whenever a player stands on top of their hitbox, using
the same detection volume and 10-tick grace period as the vanilla Happy Ghast.

## Current behavior

- Detects non-spectator players above a strider.
- Stops navigation, travel, and velocity while the strider is being used as a
  platform.
- Keeps the strider still for 10 ticks after the player leaves, matching the
  Happy Ghast's short grace period.
- Ignores players who are riding a strider normally.
- Runs on both dedicated servers and integrated single-player servers.

## Requirements

- Minecraft Java Edition 1.21.11
- Java 21
- Fabric Loader 0.19.3 or newer
- Fabric API 0.141.4+1.21.11 or newer compatible 1.21.11 build

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

This is the initial development scaffold. The project builds automatically on
every push and pull request, but the behavior should still be tested in-game
with single player, multiplayer, lava movement, leads, and strider riders.


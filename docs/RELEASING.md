# Release Process

This checklist is for project maintainers. It keeps GitHub and distribution
metadata aligned and prevents an old artifact from being silently replaced.

## 1. Prepare the release

1. Choose a new, never-before-published version number.
2. Set `mod_version` in `gradle.properties`.
3. Move the relevant `Unreleased` entries in `CHANGELOG.md` into a dated
   version heading and add comparison links at the bottom.
4. Confirm that README compatibility and dependency versions are current.
5. Review `fabric.mod.json`, including mod ID, environment, license, contact
   links, Minecraft version, Java version, and dependencies.

Never reuse a Git tag, GitHub Release version, or Modrinth version number for a
different file.

## 2. Verify locally

With the required Java version active, run:

```bash
./gradlew clean build
python3 scripts/smoke_test_server.py
```

On Windows:

```powershell
.\gradlew.bat clean build
python scripts\smoke_test_server.py
```

Confirm that:

- all GameTests and the dedicated-server smoke test pass;
- the runtime and sources JARs contain the MIT license;
- the runtime JAR contains the expected `fabric.mod.json`;
- no generated build output has been staged; and
- `git diff --check` reports no whitespace errors.

## 3. Publish on GitHub

1. Merge the prepared commit into `main`.
2. Wait for the build workflow on `main` to pass.
3. Create and push an annotated `v<version>` tag that points to the verified
   commit.
4. Wait for the release workflow to build and publish the release.
5. Verify the release contains the runtime JAR and `SHA256SUMS.txt`, that its
   notes match the changelog, and that the checksum matches the downloaded JAR.

The release workflow is the source of truth for GitHub assets. Do not upload a
locally modified replacement under the same version.

## 4. Publish on Modrinth

Before uploading, verify the Modrinth project itself:

- project title, slug, summary, license, categories, source URL, issue URL, and
  client/server environment match the current repository;
- the version targets exactly the supported Minecraft release and Fabric;
- Fabric API is a required dependency;
- the uploaded runtime JAR is byte-for-byte identical to the GitHub Release
  asset;
- the version number is new and the release notes summarize compatibility and
  upgrade requirements; and
- the project page links back to the GitHub source and issue chooser.

Fabric API's Modrinth project ID is `P7dR8mSH`.

### Existing legacy listing

The existing Modrinth project `qbzqxRbc` contains a Minecraft 1.21.11 artifact
whose internal mod ID, Java package, and license metadata predate and differ
from the current Minecraft 26.2 codebase. Do not overwrite that version or
present the current JAR as the same artifact.

Both that legacy Modrinth artifact and the current Minecraft 26.2 GitHub
Release use version number `1.0.0`. If the legacy listing is retained, the
current GitHub `1.0.0` asset cannot also be added there while preserving unique
version numbers. Publish a deliberate new GitHub version before migrating that
listing. A clean new Modrinth project may instead publish the already verified
current `1.0.0` asset.

Before publishing the current codebase on Modrinth, deliberately choose one of
these paths:

1. archive or clearly mark the old listing as legacy, then create a clean
   project for the current codebase; or
2. retain the listing only after its project metadata and migration notes make
   the incompatible identity change explicit, using a new version number.

Record the chosen migration in both the Modrinth release notes and the
changelog.

## 5. Post-release checks

- Install the downloaded release asset in a clean profile.
- Start a dedicated server with the released file.
- Check the GitHub Release links, badge, checksum, and issue chooser.
- Check the Modrinth page and download independently if a Modrinth version was
  published.
- Open a follow-up issue for anything intentionally deferred.

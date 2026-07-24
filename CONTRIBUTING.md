# Contributing to Standable Striders

Thank you for helping improve Standable Striders. Bug reports, compatibility
research, documentation fixes, tests, and focused code contributions are all
welcome.

By participating, you agree to follow the [Code of Conduct](CODE_OF_CONDUCT.md).
If you only need help installing or using the mod, see [SUPPORT.md](SUPPORT.md).
Do not report security vulnerabilities publicly; follow
[SECURITY.md](SECURITY.md).

## Before opening an issue

Use the repository's
[issue chooser](https://github.com/lestx05/lestxs-standable-striders/issues/new/choose)
and select the form that best matches your request.

- Search open and closed issues first.
- Confirm the affected Minecraft, mod, Fabric Loader, Fabric API, and Java
  versions.
- Remove duplicate or older Standable Striders JARs.
- For a bug or compatibility report, reproduce with the smallest practical mod
  set and retain `latest.log`, any crash report, and the exact mod list.

The forms request this information so that reports can be reproduced without a
long clarification cycle.

## Development setup

The current branch targets Minecraft Java Edition 26.2 and requires Java 25.
Clone your fork, create a focused branch, and let Gradle download the remaining
development dependencies.

```bash
git clone https://github.com/<your-account>/lestxs-standable-striders.git
cd lestxs-standable-striders
git switch -c fix/short-description
./gradlew build
```

On Windows PowerShell or Command Prompt, use:

```powershell
.\gradlew.bat build
```

The full build compiles the mod and runs the in-world GameTests. Run the
dedicated-server bootstrap check separately with:

```bash
python3 scripts/smoke_test_server.py
```

## Contribution guidelines

- Keep each pull request limited to one coherent change.
- Preserve the mod's focused scope and existing Happy Ghast-inspired behavior
  unless the related issue agrees on a behavior change.
- Use tabs in Java and Gradle files, matching the surrounding source.
- Add or update GameTests for gameplay changes and regressions.
- Update the README when installation, compatibility, or user-visible behavior
  changes.
- Add a concise entry under `Unreleased` in `CHANGELOG.md` for user-visible
  changes.
- Do not commit generated JARs, build directories, IDE state, credentials, or
  copied Minecraft source.
- Prefer clear commit messages that describe the outcome.

## Pull requests

Open a pull request against `main` and complete the provided template. Link the
issue it addresses, explain how the change was verified, and call out any
client/server or compatibility impact.

Before submitting:

1. Run `./gradlew build`.
2. Run `python3 scripts/smoke_test_server.py` when the change can affect startup
   or runtime loading.
3. Review `git diff` for unrelated changes and generated files.
4. Confirm that relevant documentation and the changelog are current.

Maintainers may ask for a smaller scope, additional tests, or design discussion
before merging. Opening a pull request does not guarantee acceptance.

## Licensing

The project code and original documentation are licensed under the
[MIT License](LICENSE). Unless a file states otherwise, by submitting a
contribution you agree that it may be distributed under the MIT License and
that you have the right to submit it. The
[Code of Conduct](CODE_OF_CONDUCT.md) contains adapted text under the separate
license stated in that file.

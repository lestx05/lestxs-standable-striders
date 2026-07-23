#!/usr/bin/env python3
"""Extract one version section from CHANGELOG.md for GitHub Releases."""

from __future__ import annotations

from pathlib import Path
import re
import sys


VERSION_HEADING = re.compile(
	r"^## \[(?P<version>[^\]]+)\](?:\s+-\s+.+)?\s*$"
)


def extract_release_notes(changelog: str, version: str) -> str:
	lines = changelog.splitlines()
	start: int | None = None

	for index, line in enumerate(lines):
		match = VERSION_HEADING.match(line)
		if match and match.group("version") == version:
			start = index + 1
			break

	if start is None:
		raise ValueError(f"Version {version!r} is missing from the changelog")

	end = len(lines)
	for index in range(start, len(lines)):
		if lines[index].startswith("## "):
			end = index
			break

	notes = "\n".join(lines[start:end]).strip()
	if not notes:
		raise ValueError(f"Version {version!r} has no release notes")

	return notes


def main() -> int:
	if len(sys.argv) != 3:
		print(
			"Usage: extract_release_notes.py CHANGELOG.md VERSION",
			file=sys.stderr,
		)
		return 2

	changelog_path = Path(sys.argv[1])
	version = sys.argv[2]

	try:
		notes = extract_release_notes(
			changelog_path.read_text(encoding="utf-8"),
			version,
		)
	except (OSError, ValueError) as error:
		print(error, file=sys.stderr)
		return 1

	print(notes)
	return 0


if __name__ == "__main__":
	raise SystemExit(main())

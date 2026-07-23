#!/usr/bin/env python3
"""Start the Fabric development server and verify that Minecraft bootstraps."""

from __future__ import annotations

import os
from pathlib import Path
import shutil
import signal
import subprocess
import sys
import time


REPOSITORY_ROOT = Path(__file__).resolve().parents[1]
RUN_DIRECTORY = REPOSITORY_ROOT / "run"
LATEST_LOG = RUN_DIRECTORY / "logs" / "latest.log"
PROCESS_LOG = RUN_DIRECTORY / "server-smoke-output.log"
STARTUP_TIMEOUT_SECONDS = 120
IS_WINDOWS = os.name == "nt"
GRADLE_WRAPPER = REPOSITORY_ROOT / ("gradlew.bat" if IS_WINDOWS else "gradlew")


def stop_process_group(process: subprocess.Popen[str]) -> None:
	if process.poll() is not None:
		return

	if IS_WINDOWS:
		subprocess.run(
			["taskkill", "/PID", str(process.pid), "/T", "/F"],
			check=False,
			stdout=subprocess.DEVNULL,
			stderr=subprocess.DEVNULL,
		)
	else:
		os.killpg(process.pid, signal.SIGTERM)

	try:
		process.wait(timeout=10)
	except subprocess.TimeoutExpired:
		if IS_WINDOWS:
			process.kill()
		else:
			os.killpg(process.pid, signal.SIGKILL)
		process.wait(timeout=10)


def print_process_log() -> None:
	if not PROCESS_LOG.exists():
		return

	lines = PROCESS_LOG.read_text(encoding="utf-8", errors="replace").splitlines()
	print("\n".join(lines[-200:]), file=sys.stderr)


def main() -> int:
	shutil.rmtree(RUN_DIRECTORY, ignore_errors=True)
	RUN_DIRECTORY.mkdir(parents=True)
	(RUN_DIRECTORY / "eula.txt").write_text("eula=true\n", encoding="utf-8")

	process_options: dict[str, object] = {}
	if IS_WINDOWS:
		process_options["creationflags"] = subprocess.CREATE_NEW_PROCESS_GROUP
	else:
		process_options["start_new_session"] = True

	with PROCESS_LOG.open("w", encoding="utf-8") as output:
		process = subprocess.Popen(
			[
				str(GRADLE_WRAPPER),
				"runServer",
				"--args=nogui",
				"--no-daemon",
			],
			stdin=subprocess.DEVNULL,
			stdout=output,
			stderr=subprocess.STDOUT,
			text=True,
			cwd=REPOSITORY_ROOT,
			**process_options,
		)

		deadline = time.monotonic() + STARTUP_TIMEOUT_SECONDS
		while time.monotonic() < deadline:
			return_code = process.poll()
			if return_code is not None:
				output.flush()
				print(f"Server exited before bootstrap completed (code {return_code}).", file=sys.stderr)
				print_process_log()
				return 1

			if LATEST_LOG.exists():
				log_text = LATEST_LOG.read_text(encoding="utf-8", errors="replace")
				if "Done (" in log_text:
					stop_process_group(process)
					print("Dedicated server bootstrap completed successfully.")
					return 0

			time.sleep(1)

		stop_process_group(process)
		output.flush()
		print(f"Server did not finish bootstrap within {STARTUP_TIMEOUT_SECONDS} seconds.", file=sys.stderr)
		print_process_log()
		return 1


if __name__ == "__main__":
	raise SystemExit(main())

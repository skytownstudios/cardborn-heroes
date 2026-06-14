#!/usr/bin/env python3
"""Append rebuild session metrics to docs/REBUILD_SESSION.log (CPU/RAM/disk)."""

from __future__ import annotations

import argparse
import shutil
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent
LOG = ROOT / "docs" / "REBUILD_SESSION.log"


def metrics() -> tuple[float, float, float, float]:
    raw = subprocess.check_output(
        [
            "powershell",
            "-NoProfile",
            "-Command",
            "$c=(Get-CimInstance Win32_Processor).LoadPercentage;"
            "$o=Get-CimInstance Win32_OperatingSystem;"
            "$u=[math]::Round(($o.TotalVisibleMemorySize-$o.FreePhysicalMemory)/1MB,2);"
            "$t=[math]::Round($o.TotalVisibleMemorySize/1MB,2);"
            "$d=[math]::Round((Get-PSDrive C).Free/1GB,2);"
            "$c,$u,$t,$d -join ','",
        ],
        text=True,
    ).strip()
    parts = [float(x) for x in raw.split(",")]
    return parts[0], parts[1], parts[2], parts[3]


def log(phase: str, message: str) -> None:
    cpu, ru, rt, df = metrics()
    line = (
        f"{datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M:%S UTC')} | "
        f"phase={phase} | {message} | "
        f"cpu={cpu:.0f}% ram={ru:.1f}/{rt:.1f}GB disk_free={df:.0f}GB"
    )
    LOG.parent.mkdir(parents=True, exist_ok=True)
    with LOG.open("a", encoding="utf-8") as f:
        f.write(line + "\n")
    print(line)
    if cpu > 90:
        print(f"WARNING: CPU {cpu:.0f}% — pause before next heavy step", file=sys.stderr)
    if rt - ru < 2:
        print(f"WARNING: low RAM ({rt - ru:.1f} GB free)", file=sys.stderr)


def main() -> None:
    p = argparse.ArgumentParser()
    p.add_argument("--phase", required=True)
    p.add_argument("--message", required=True)
    args = p.parse_args()
    log(args.phase, args.message)


if __name__ == "__main__":
    main()

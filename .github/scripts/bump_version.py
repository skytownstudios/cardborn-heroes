#!/usr/bin/env python3
"""Bump MARKETING_VERSION and CURRENT_PROJECT_VERSION in project.yml."""

from __future__ import annotations

import argparse
import re
from pathlib import Path


def bump(project_yml: Path, marketing: str | None, build: str | None) -> None:
    text = project_yml.read_text(encoding="utf-8")
    if marketing:
        text, n = re.subn(
            r"MARKETING_VERSION:\s*\"[^\"]+\"",
            f'MARKETING_VERSION: "{marketing}"',
            text,
            count=1,
        )
        if n == 0:
            raise SystemExit("MARKETING_VERSION not found in project.yml")
    if build:
        text, n = re.subn(
            r"CURRENT_PROJECT_VERSION:\s*\"[^\"]+\"",
            f'CURRENT_PROJECT_VERSION: "{build}"',
            text,
            count=1,
        )
        if n == 0:
            raise SystemExit("CURRENT_PROJECT_VERSION not found in project.yml")
    project_yml.write_text(text, encoding="utf-8")
    print(f"Updated {project_yml}")


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument("--marketing", help='e.g. "1.0.1"')
    p.add_argument("--build", help='e.g. "2" (CFBundleVersion)')
    p.add_argument("--project", default="project.yml")
    args = p.parse_args()
    if not args.marketing and not args.build:
        p.error("Pass --marketing and/or --build")
    bump(Path(args.project).resolve(), args.marketing, args.build)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

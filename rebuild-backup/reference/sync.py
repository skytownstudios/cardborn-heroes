#!/usr/bin/env python3
"""Copy shared/content and shared/assets into iOS and Android bundles."""

from __future__ import annotations

import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent


def find_ios_app_dir(ios_root: Path) -> Path | None:
    for child in ios_root.iterdir():
        if child.is_dir() and (child / f"{child.name}App.swift").exists():
            return child
    return None


def sync(root: Path) -> None:
    shared_content = root / "shared" / "content"
    shared_assets = root / "shared" / "assets"
    ios_root = root / "ios"
    android_assets = root / "android" / "app" / "src" / "main" / "assets"

    app_dir = find_ios_app_dir(ios_root)
    if app_dir and shared_content.is_dir():
        ios_content = app_dir / "Content"
        ios_content.mkdir(parents=True, exist_ok=True)
        for item in shared_content.iterdir():
            if item.is_file():
                shutil.copy2(item, ios_content / item.name)

    if app_dir and shared_assets.is_dir():
        ios_content = app_dir / "Content"
        ios_content.mkdir(parents=True, exist_ok=True)
        for item in shared_assets.rglob("*"):
            if item.is_file():
                rel = item.relative_to(shared_assets)
                dest = ios_content / rel
                dest.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(item, dest)

    android_assets.mkdir(parents=True, exist_ok=True)
    if shared_content.is_dir():
        dest_content = android_assets / "content"
        dest_content.mkdir(parents=True, exist_ok=True)
        for item in shared_content.iterdir():
            if item.is_file():
                shutil.copy2(item, dest_content / item.name)

    if shared_assets.is_dir():
        for item in shared_assets.rglob("*"):
            if item.is_file():
                rel = item.relative_to(shared_assets)
                dest = android_assets / rel
                dest.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(item, dest)


if __name__ == "__main__":
    if not (ROOT / "shared" / "config" / "app.json").is_file():
        print("Error: run from monorepo root (needs shared/config/app.json)", file=sys.stderr)
        sys.exit(1)
    sync(ROOT)
    print(f"Synced shared -> ios + android ({ROOT})")

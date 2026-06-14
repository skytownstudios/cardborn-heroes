#!/usr/bin/env python3
"""Copy shared/content and shared/assets into ios and android bundles."""

from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent
sys.path.insert(0, str(ROOT.parent / ".cursor" / "skills" / "_shared"))
if not (ROOT.parent / ".cursor" / "skills" / "_shared").exists():
    sys.path.insert(0, str(Path.home() / ".cursor" / "skills" / "_shared"))
from monorepo_util import is_monorepo, sync_shared_to_platforms  # noqa: E402

if __name__ == "__main__":
    root = ROOT
    if not is_monorepo(root):
        print("Error: run from monorepo root (needs shared/config/app.json)", file=sys.stderr)
        sys.exit(1)
    sync_shared_to_platforms(root)
    print(f"Synced shared -> ios + android ({root})")

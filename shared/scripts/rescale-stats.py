#!/usr/bin/env python3
"""Rescale hero and gear stats to normalized power (basic 0-star recruit = 10)."""
import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TIER_ORDER = ["basic", "common", "uncommon", "rare", "epic", "mythic", "cardborn"]
STAR_BASE = 1.5

ROLE_TEMPLATES = {
    "tank": {"hp": 5, "atk": 2, "def": 3, "energy": 0},
    "ranger": {"hp": 3, "atk": 4, "def": 3, "energy": 0},
    "caster": {"hp": 2, "atk": 5, "def": 3, "energy": 0},
}

GEAR_TEMPLATES = {
    "sword": {"atk": 2, "hp": 0, "def": 1, "energy": 0},
    "shield": {"atk": 0, "hp": 1, "def": 2, "energy": 0},
    "bow": {"atk": 3, "hp": 0, "def": 0, "energy": 0},
    "staff": {"atk": 2, "hp": 0, "def": 0, "energy": 1},
}


def tier_mult(tier: str) -> float:
    idx = TIER_ORDER.index(tier) if tier in TIER_ORDER else 0
    return STAR_BASE ** (idx * 5)


def scale_template(template: dict, mult: float) -> dict:
    out = {}
    for k, v in template.items():
        if k == "energy":
            out[k] = max(0, round(v * mult))
        else:
            out[k] = max(1, round(v * mult)) if v > 0 else 0
    return out


def hero_stats(role: str, tier: str, unit_kind: str) -> dict:
    base = ROLE_TEMPLATES.get(role, ROLE_TEMPLATES["tank"])
    mult = tier_mult(tier)
    if unit_kind == "hero" and tier == "basic":
        mult *= STAR_BASE
    return scale_template(base, mult)


def gear_kind(gear_id: str) -> str:
    for kind in GEAR_TEMPLATES:
        if kind in gear_id:
            return kind
    return "sword"


def gear_bonus(gear_id: str, tier: str) -> dict:
    kind = gear_kind(gear_id)
    return scale_template(GEAR_TEMPLATES[kind], tier_mult(tier))


def main():
    heroes_path = ROOT / "content" / "heroes.json"
    gear_path = ROOT / "content" / "gear.json"

    heroes_data = json.loads(heroes_path.read_text(encoding="utf-8"))
    for hero in heroes_data["heroes"]:
        hero["stats"] = hero_stats(
            hero.get("role", "tank"),
            hero.get("tier", "basic"),
            hero.get("unitKind", "hero"),
        )
    heroes_path.write_text(json.dumps(heroes_data, indent=2) + "\n", encoding="utf-8")

    gear_data = json.loads(gear_path.read_text(encoding="utf-8"))
    for item in gear_data["gear"]:
        item["bonus"] = gear_bonus(item["id"], item.get("tier", "basic"))
    gear_path.write_text(json.dumps(gear_data, indent=2) + "\n", encoding="utf-8")

    print("Rescaled heroes.json and gear.json")


if __name__ == "__main__":
    main()

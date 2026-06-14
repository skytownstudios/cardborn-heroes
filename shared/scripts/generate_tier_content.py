#!/usr/bin/env python3
"""Generate tiered heroes/gear + Ascendant pack definitions."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CONTENT = ROOT / "content"

PACK_TIERS = ["basic", "common", "uncommon", "rare", "epic", "mythic"]
DROP_TIERS = PACK_TIERS + ["cardborn"]
MULT = {
    "basic": 1.0,
    "common": 1.15,
    "uncommon": 1.32,
    "rare": 1.52,
    "epic": 1.75,
    "mythic": 2.0,
    "cardborn": 2.35,
}

HERO_BASE = [
    {
        "suffix": "knight",
        "name": "Knight",
        "role": "tank",
        "art": "cards/knight.png",
        "battleArt": "cards/knight_battle.png",
        "stats": {"hp": 1200, "atk": 85, "def": 60},
        "description": "Frontline shield bearer.",
    },
    {
        "suffix": "archer",
        "name": "Archer",
        "role": "ranger",
        "art": "cards/archer.png",
        "battleArt": "cards/archer_battle.png",
        "stats": {"hp": 750, "atk": 110, "def": 35},
        "description": "Ranged striker.",
    },
    {
        "suffix": "mage",
        "name": "Mage",
        "role": "caster",
        "art": "cards/mage.png",
        "battleArt": "cards/mage_battle.png",
        "stats": {"hp": 680, "atk": 130, "def": 25},
        "description": "Arcane damage dealer.",
    },
]

GEAR_BASE = [
    {
        "suffix": "sword",
        "name": "Sword",
        "slot": "weapon",
        "art": "cards/sword.png",
        "bonus": {"atk": 25, "def": 10, "hp": 0},
        "compatibleRoles": ["tank", "ranger"],
        "description": "A sturdy blade.",
    },
    {
        "suffix": "bow",
        "name": "Bow",
        "slot": "weapon",
        "art": "cards/bow.png",
        "bonus": {"atk": 35, "def": 0, "hp": 50},
        "compatibleRoles": ["ranger"],
        "description": "Standard ranged weapon.",
    },
    {
        "suffix": "staff",
        "name": "Staff",
        "slot": "weapon",
        "art": "cards/staff.png",
        "bonus": {"atk": 40, "def": 0, "hp": 30},
        "compatibleRoles": ["caster"],
        "description": "Focuses arcane power.",
    },
]

TIER_LABEL = {
    "basic": "Basic",
    "common": "Common",
    "uncommon": "Uncommon",
    "rare": "Rare",
    "epic": "Epic",
    "mythic": "Mythic",
    "cardborn": "Cardborn",
}


def scale_stats(stats: dict, mult: float) -> dict:
    return {k: max(1, int(v * mult)) for k, v in stats.items()}


def card_id(prefix: str, suffix: str, tier: str) -> str:
    if tier == "basic":
        return f"{prefix}_{suffix}"
    return f"{prefix}_{suffix}_{tier}"


def main() -> None:
    heroes = []
    gear = []
    for tier in DROP_TIERS:
        label = TIER_LABEL[tier]
        mult = MULT[tier]
        for base in HERO_BASE:
            heroes.append(
                {
                    "id": card_id("hero", base["suffix"], tier),
                    "name": f"{label} {base['name']}",
                    "role": base["role"],
                    "tier": tier,
                    "rarity": tier,
                    "art": base["art"],
                    "battleArt": base["battleArt"],
                    "stats": scale_stats(base["stats"], mult),
                    "description": f"{label}-tier {base['description']}",
                }
            )
        for base in GEAR_BASE:
            bonus = scale_stats(base["bonus"], mult)
            gear.append(
                {
                    "id": card_id("gear", base["suffix"], tier),
                    "name": f"{label} {base['name']}",
                    "slot": base["slot"],
                    "tier": tier,
                    "rarity": tier,
                    "art": base["art"],
                    "bonus": {"atk": bonus["atk"], "hp": bonus["hp"], "def": bonus.get("def", 0)},
                    "compatibleRoles": base["compatibleRoles"],
                    "description": f"{label}-tier {base['description']}",
                }
            )

    packs = []
    crown_costs = [500, 1200, 2500, 0, 0, 0]
    sigil_costs = [0, 0, 0, 150, 350, 750]
    for tier, crowns, sigils in zip(PACK_TIERS, crown_costs, sigil_costs):
        packs.append(
            {
                "id": f"ascendant_{tier}",
                "name": f"{TIER_LABEL[tier]} Ascendant Pack",
                "packLine": "ascendant",
                "tier": tier,
                "description": (
                    f"Ascendant pack — 8 mixed cards/materials ({tier} or lower), "
                    f"1 guaranteed {tier}+ slot (hero or gear)."
                ),
                "currency": "sigils" if crowns == 0 else "crowns",
                "cost": sigils if crowns == 0 else crowns,
            }
        )

    CONTENT.mkdir(parents=True, exist_ok=True)
    (CONTENT / "heroes.json").write_text(json.dumps({"heroes": heroes}, indent=2) + "\n", encoding="utf-8")
    (CONTENT / "gear.json").write_text(json.dumps({"gear": gear}, indent=2) + "\n", encoding="utf-8")
    (CONTENT / "packs.json").write_text(json.dumps({"packs": packs}, indent=2) + "\n", encoding="utf-8")
    print(f"Wrote {len(heroes)} heroes, {len(gear)} gear, {len(packs)} ascendant packs")


if __name__ == "__main__":
    main()

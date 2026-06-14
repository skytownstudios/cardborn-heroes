#!/usr/bin/env python3
"""Generate Gilded Ivory placeholder PNG assets until real art is ready."""

from __future__ import annotations

from pathlib import Path

try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    raise SystemExit("Install Pillow: py -m pip install Pillow")

ROOT = Path(__file__).resolve().parent.parent.parent
ASSETS = ROOT / "shared" / "assets"

CREAM = (245, 240, 230)
GOLD = (212, 175, 55)
BRONZE = (176, 141, 87)
CHARCOAL = (59, 47, 47)


def save_card(path: Path, label: str, accent: tuple[int, int, int]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    img = Image.new("RGBA", (512, 512), CREAM + (255,))
    draw = ImageDraw.Draw(img)
    draw.rounded_rectangle((32, 32, 480, 480), radius=24, outline=GOLD, width=6, fill=(255, 255, 255, 255))
    draw.rounded_rectangle((64, 64, 448, 400), radius=16, fill=accent + (180,))
    draw.text((256, 440), label, fill=CHARCOAL, anchor="mm")
    img.save(path)


def save_icon(path: Path, label: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.ellipse((8, 8, 120, 120), fill=GOLD + (255,), outline=BRONZE, width=3)
    draw.text((64, 64), label[:3], fill=CHARCOAL, anchor="mm")
    img.save(path)


def main() -> None:
    heroes = ["knight", "archer", "mage"]
    gear = ["sword", "bow", "staff"]
    tiers = ["", "_common", "_uncommon", "_rare", "_epic", "_mythic", "_cardborn"]

    for h in heroes:
        save_card(ASSETS / "cards" / f"{h}.png", h.title(), GOLD)
        save_card(ASSETS / "cards" / f"{h}_battle.png", h.title(), BRONZE)
        for t in tiers[1:]:
            save_card(ASSETS / "cards" / f"{h}{t}.png", h.title(), GOLD)

    for g in gear:
        save_card(ASSETS / "cards" / f"{g}.png", g.title(), BRONZE)
        for t in tiers[1:]:
            save_card(ASSETS / "cards" / f"{g}{t}.png", g.title(), BRONZE)

    save_icon(ASSETS / "ui" / "currency_crowns.png", "Cr")
    save_icon(ASSETS / "ui" / "currency_sigils.png", "Si")
    save_icon(ASSETS / "ui" / "mat_hero_essence.png", "Es")
    save_icon(ASSETS / "ui" / "mat_steel.png", "St")
    save_icon(ASSETS / "ui" / "mat_arcane_dust.png", "Du")
    for tier in ["basic", "common", "uncommon", "rare", "epic", "mythic"]:
        save_icon(ASSETS / "ui" / f"pack_ascendant_{tier}.png", tier[:2].upper())

    print(f"Placeholder assets written under {ASSETS}")


if __name__ == "__main__":
    main()

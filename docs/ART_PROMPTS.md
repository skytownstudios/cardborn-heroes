# Cardborn Heroes — Art direction (Style C locked)

## Chosen style: Western Cartoon RPG (Style C)

**Reference:** Hearthstone / western MMO hero icons — round friendly shapes, colorful armor, warm gold/cream accents, illustrated but not busy.

**Avoid:** anime, manga, chibi, tier/rank text in images, overly detailed rigs.

**Tier badges & frames:** UI code only (`CardArt.kt` borders). One portrait + one rig set per hero role, shared across all tiers.

**Prompt suffix (all assets):**

> Western cartoon RPG game art, Hearthstone-style hero icon, round friendly shapes, colorful armor, warm gold cream accents, illustrated but not busy, NOT anime NOT manga, NO text NO labels, game asset

---

## Hero assets

| Role | Portrait | Battle rig |
|------|----------|------------|
| knight | `heroes/knight/portrait.png` | `heroes/knight/rig/{legs,torso,arm_left,arm_right,head,weapon}.png` |
| archer | `heroes/archer/portrait.png` | `heroes/archer/rig/*.png` |
| mage | `heroes/mage/portrait.png` | `heroes/mage/rig/*.png` |

Rig stack order: legs → torso → arm_left → arm_right → head → weapon. Animated in `BattleRigView.kt` (attack on each combat tick).

---

## Full asset manifest (55 files)

See folders: `heroes/`, `gear/`, `ui/`, `maps/`, `battle/`, `shop/`, `forge/`

Install pipeline:

```powershell
powershell -File shared/scripts/install-generated-art.ps1
node shared/scripts/sync.js
```

Generate new art into Cursor assets folder using flat filenames from `install-generated-art.ps1` mapping.

---

## Obsolete

Do not use `shared/assets/cards/` tier-variant placeholders. Content JSON uses `heroes/{role}/portrait.png` and `gear/{item}.png`.

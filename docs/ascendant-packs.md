# Ascendant Pack drop statistics

Reference for the **Ascendant Pack** booster line (`packLine: "ascendant"`).  
Implementation: `android/.../data/PackGenerator.kt`.

---

## Pack tiers (purchasable)

| ID | Name | Cost |
|----|------|------|
| `ascendant_basic` | Basic Ascendant Pack | 500 Crowns |
| `ascendant_common` | Common Ascendant Pack | 1,200 Crowns |
| `ascendant_uncommon` | Uncommon Ascendant Pack | 2,500 Crowns |
| `ascendant_rare` | Rare Ascendant Pack | 150 Sigils |
| `ascendant_epic` | Epic Ascendant Pack | 350 Sigils |
| `ascendant_mythic` | Mythic Ascendant Pack | 750 Sigils |

**Drop-only tier:** `cardborn` (highest). Not sold as a pack. Only from Mythic pack slot 9.

---

## Pack contents (9 cards)

| Slots | Contents |
|-------|----------|
| **1–8** | 3 heroes + 3 gear + 2 crafting materials (order shuffled). Tier ≤ pack tier. |
| **9** | Guaranteed **hero or gear** at pack tier, or small upgrade chance (see below). |

Slot 9: **50% hero / 50% gear** (never material).

Within a tier pool, each hero or gear piece has equal weight (3 heroes, 3 gear per tier).

---

## Tier weights — slots 1–8

Each of the 8 regular slots rolls tier independently.

1. The pack's own tier uses a fixed target rate (`TOP_TIER_SLOT_RATE` in code).
2. All lower tiers split the remainder using geometric weights `4^(packIndex − tierIndex)`.

| Pack | Own-tier target (per slot) |
|------|---------------------------|
| Basic | 100% |
| Common | 25% |
| Uncommon | 12% |
| Rare | 9% |
| Epic | 7% |
| Mythic | **6%** |

---

## Slot 9 — guaranteed + upgrade

`GUARANTEED_UPGRADE_CHANCE = [0.40, 0.28, 0.20, 0.14, 0.08, 0.025]`

| Pack | Guaranteed | Upgrade → |
|------|------------|-------------|
| Basic | 60% basic | 40% common |
| Common | 72% common | 28% uncommon |
| Uncommon | 80% uncommon | 20% rare |
| Rare | 86% rare | 14% epic |
| Epic | 92% epic | 8% mythic |
| Mythic | 97.5% mythic | **2.5% cardborn** |

Material qty in packs: `3 × tier rank` (basic=3 … mythic=18).

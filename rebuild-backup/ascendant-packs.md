# Ascendant Pack drop statistics

Reference for the **Ascendant Pack** booster line (`packLine: "ascendant"`).  
Implementation: `android/.../data/PackGenerator.kt`.

Other pack lines (future) will have their own docs and rules.

---

## Pack tiers (purchasable)

| ID | Name | Cost |
|----|------|------|
| `ascendant_basic` | Basic Ascendant Pack | 500 gold |
| `ascendant_common` | Common Ascendant Pack | 1,200 gold |
| `ascendant_uncommon` | Uncommon Ascendant Pack | 2,500 gold |
| `ascendant_rare` | Rare Ascendant Pack | 150 gems |
| `ascendant_epic` | Epic Ascendant Pack | 350 gems |
| `ascendant_mythic` | Mythic Ascendant Pack | 750 gems |

**Drop-only tier:** `cardborn` (highest). Not sold as a pack. Only from Mythic pack slot 9.

---

## Pack contents (9 cards)

| Slots | Contents |
|-------|----------|
| **1–8** | 3 heroes + 3 gear + 2 crafting materials (order shuffled). Tier ≤ pack tier. |
| **9** | Guaranteed **hero or gear** at pack tier, or small upgrade chance (see below). |

### Slot type odds (slots 1–8, each slot)

| Type | Chance |
|------|--------|
| Hero | 37.5% (3/8) |
| Equipment | 37.5% (3/8) |
| Crafting material | 25% (2/8) |

Slot 9: **50% hero / 50% gear** (never material).

Within a tier pool, each hero or gear piece has equal weight (3 heroes, 3 gear per tier).

---

## Tier weights — slots 1–8

Each of the 8 regular slots rolls tier independently.

1. The **pack’s own tier** uses a fixed target rate (`TOP_TIER_SLOT_RATE` in code).
2. All **lower tiers** split the remainder using geometric weights `4^(packIndex − tierIndex)`.

| Pack | Own-tier target (per slot) |
|------|---------------------------|
| Basic | 100% |
| Common | 25% |
| Uncommon | 12% |
| Rare | 9% |
| Epic | 7% |
| Mythic | **6%** (within 5–7% design band) |

### Tier roll table (slots 1–8)

Percentages below are **per slot**. Multiply hero/gear chance (37.5%) for card-type odds.

#### Basic Ascendant

| Basic |
|------:|
| 100.0% |

#### Common Ascendant

| Basic | Common |
|------:|-------:|
| 75.0% | 25.0% |

#### Uncommon Ascendant

| Basic | Common | Uncommon |
|------:|-------:|---------:|
| 70.4% | 17.6% | 12.0% |

#### Rare Ascendant

| Basic | Common | Uncommon | Rare |
|------:|-------:|---------:|-----:|
| 69.3% | 17.3% | 4.3% | 9.0% |

#### Epic Ascendant

| Basic | Common | Uncommon | Rare | Epic |
|------:|-------:|---------:|-----:|-----:|
| 70.0% | 17.5% | 4.4% | 1.1% | 7.0% |

#### Mythic Ascendant

| Basic | Common | Uncommon | Rare | Epic | Mythic |
|------:|-------:|---------:|-----:|-----:|-------:|
| 70.6% | 17.6% | 4.4% | 1.1% | 0.3% | **6.0%** |

**Example — Mythic hero on a regular slot:** 37.5% × 6.0% = **2.25% per slot** (~17% chance of at least one mythic hero/gear across 8 regular slots).

Cardborn **never** appears in slots 1–8.

---

## Slot 9 — guaranteed hero or gear

| Pack | Pack tier | Upgrade chance | Upgrade result |
|------|----------:|---------------:|----------------|
| Basic | 60.0% basic | 40.0% | common |
| Common | 72.0% common | 28.0% | uncommon |
| Uncommon | 80.0% uncommon | 20.0% | rare |
| Rare | 86.0% rare | 14.0% | epic |
| Epic | 92.0% epic | 8.0% | mythic |
| Mythic | 97.5% mythic | **2.5%** | **cardborn** |

Slot 9 is 50% hero / 50% gear:

| Outcome (Mythic pack only) | Per pack |
|----------------------------|----------|
| Any Cardborn | 2.5% |
| Cardborn hero | 1.25% |
| Cardborn gear | 1.25% |

---

## Crafting materials (slots 1–8 only)

- Material **type**: random from full materials pool.
- Material **quantity** = `3 × tier rank` (basic=3, common=6, … mythic=18).
- Tier for qty follows the same tier roll as slots 1–8 (never Cardborn).

---

## Tuning constants (code)

Edit these in `PackGenerator.kt` to rebalance:

```kotlin
// Per-slot rate for pack's own tier on slots 1–8
TOP_TIER_SLOT_RATE = floatArrayOf(1.0f, 0.25f, 0.12f, 0.09f, 0.07f, 0.06f)

// Slot 9 upgrade to next tier (Mythic → Cardborn)
GUARANTEED_UPGRADE_CHANCE = floatArrayOf(0.40f, 0.28f, 0.20f, 0.14f, 0.08f, 0.025f)

// Lower-tier decay multiplier
TIER_WEIGHT_BASE = 4
```

After changing constants, update the tier tables in this doc.

---

## Quick summary

| Pack | Regular slots feel | Slot 9 | Rarest pull |
|------|-------------------|--------|-------------|
| Basic | All basic | basic / common | Common |
| Common | Mostly basic | common / uncommon | Uncommon |
| Uncommon | Mostly basic | uncommon / rare | Rare |
| Rare | Mostly basic–common | rare / epic | Epic |
| Epic | Mostly basic–uncommon | epic / mythic | Mythic |
| Mythic | Mostly filler + ~6% mythic/slot | mythic / **cardborn** | **Cardborn** |

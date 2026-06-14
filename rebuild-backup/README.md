# Rebuild backup — copy this folder away before deleting the repo

Created so you can wipe `cardborn-heroes` and rebuild from scratch using **REBUILD_PLAN.md**.

## What's inside

| Path | Purpose |
|------|---------|
| **REBUILD_PLAN.md** | Full rebuild blueprint (navigation, stats, phases, art prompts) |
| **ascendant-packs.md** | Ascendant Pack drop rate reference |
| **reference/** | Code to port when rebuilding |
| **content/** | Snapshot of `shared/content/*.json` (heroes, gear, packs, etc.) |

### reference/

- `app.json` — app IDs, IAP, AdMob
- `generate_tier_content.py` — tier stat generator
- `sync.py` — shared → iOS/Android sync
- `PackGenerator.kt` — Ascendant pack logic
- `AscendantPackOdds.kt` — shop odds strings
- `BattleScreen.kt` — staggered fight layout (keep as-is)
- `AppTheme.kt` — Gilded Ivory hex colors

## How to use

1. Copy the entire **`rebuild-backup`** folder to Desktop, cloud drive, or another repo.
2. Delete everything in `cardborn-heroes` except what you want to keep (or delete the whole folder).
3. Start fresh repo; paste `REBUILD_PLAN.md` → `docs/REBUILD_PLAN.md`.
4. Execute phases 0–11 from the plan with iOS + Android parity.

## Locked decisions (from planning session)

- **Currencies:** Crowns (soft) + Sigils (premium)
- **Tabs:** Battle | Hand | Cards | Forge | Shop + Vault/Bag from top bar
- **Hands:** 3 loadouts, 5 slots each, active Hand drives farm + campaign
- **Battle hub:** AFK-style map + farm picker + Hand preview + Begin
- **Build:** iOS and Android parity every phase

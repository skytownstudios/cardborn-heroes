# Cardborn Heroes

Dual-platform hero card collector (Android + iOS). Shared content in `shared/`.

## Quick start

```powershell
node shared/scripts/generate_tier_content.js
powershell -File shared/scripts/generate-placeholder-assets.ps1
node shared/scripts/sync.js
cd android
.\gradlew.bat assembleDebug
```

## Deploy (Pixel 6)

From `phone-emulator`:

```powershell
.\phone.ps1 pixel-6 -Install
```

## Docs

- [REBUILD_PLAN.md](docs/REBUILD_PLAN.md)
- [ascendant-packs.md](docs/ascendant-packs.md)
- [ART_PROMPTS.md](docs/ART_PROMPTS.md)
- Session log: [REBUILD_SESSION.log](docs/REBUILD_SESSION.log)

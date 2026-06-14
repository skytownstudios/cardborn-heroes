# Cardborn Heroes

Dual-platform kids educational app (iOS + Android).

## Structure

- `shared/content/` — edit lessons and game JSON once
- `shared/assets/audio/` — add sound files once
- `ios/` — SwiftUI (build on Mac / GitHub Actions)
- `android/` — Kotlin Compose (build on Windows / GitHub Actions)

## Sync content to platforms

```bash
python shared/scripts/sync.py
```

Or re-run `kids-app-pipeline` shared-sync step.

## Build

**Android (Windows):** open `android/` in Android Studio → Run

**iOS (Mac):** `cd ios && xcodegen generate && open *.xcodeproj`

**CI:** push to `main` — workflows build both platforms.

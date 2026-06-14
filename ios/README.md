# Cardborn Heroes (iOS)

SwiftUI app shell with AdMob and remove-ads IAP.

## Develop on Windows

Edit Swift in Cursor. Push to GitHub — **Actions** compiles on a cloud Mac (no local Mac required for CI).

## Build on a Mac

See [README-SETUP.md](README-SETUP.md).

## Quick start (Mac)

```bash
cp Config/Secrets.xcconfig.example Config/Secrets.xcconfig
brew install xcodegen && xcodegen generate
open CardbornHeroes.xcodeproj
```

## GitHub

- CI workflow: [.github/workflows/ios-build.yml](.github/workflows/ios-build.yml)
- Green check on `main` = project compiles
- Game code: `CardbornHeroes/Features/Home/HomeView.swift`

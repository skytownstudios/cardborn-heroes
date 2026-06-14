# Cardborn Heroes — iOS shell setup

Scaffolded shell: navigation, settings, AdMob interstitial support, and StoreKit remove-ads IAP. **Game content goes in `Features/Home/HomeView.swift`.** Optional floating ad fairy: use the `ios-ad-fairy` skill.

## Requirements

- macOS with Xcode 15+ (build/sign is not possible on Windows alone)
- [XcodeGen](https://github.com/yonaskolb/XcodeGen): `brew install xcodegen`
- Apple Developer account for device testing and App Store
- App Store Connect: create non-consumable IAP `com.skytownstudios.cardbornheroes.remove_ads`

## Generate Xcode project (on Mac)

```bash
cd /path/to/this/folder
cp Config/Secrets.xcconfig.example Config/Secrets.xcconfig
# Edit Secrets.xcconfig with your real AdMob IDs
xcodegen generate
open CardbornHeroes.xcodeproj
```

In Xcode:

1. Set your **Team** under Signing & Capabilities.
2. Add capability **In-App Purchase** (if not auto-detected).
3. Product → Scheme → Edit Scheme → Run → Options: StoreKit Configuration optional for local IAP tests (`Products.storekit` if you add one).
4. Build and run on simulator or device.

## AdMob

- Replace test IDs in `Config/Secrets.xcconfig` before release.
- `Info.plist` references `$(ADMOB_APP_ID)` — wire `Secrets.xcconfig` in Xcode build settings if needed.
- For **Kids** category apps: review Apple Kids guidelines and AdMob child-directed settings in [reference.md](reference.md) (in skill folder).

## IAP (remove ads)

- Product ID: `com.skytownstudios.cardbornheroes.remove_ads`
- Type: **Non-consumable**
- Price: e.g. $4.99
- Settings screen includes **Restore Purchases**.

## Where to add your game

Edit `CardbornHeroes/Features/Home/HomeView.swift` — replace `GamePlaceholderView` with your gameplay.

## Windows development

Edit Swift in Cursor on Windows; push to GitHub — **Actions** runs `.github/workflows/ios-build.yml` on a cloud Mac (free for public repos).

```powershell
git add -A
git commit -m "Your message"
git push
```

Commit before push. See skill `reference-github.md` if push or CI fails.

## Windows development (Mac builds)

Sync via Git; open on Mac for Simulator: `git clone` → `xcodegen generate` → Xcode Run.

# Release process — Cardborn Heroes

## Version bump

```bash
python .github/scripts/bump_version.py --marketing 1.0.1 --build 2
```

Updates `project.yml` (`MARKETING_VERSION`, `CURRENT_PROJECT_VERSION`).

## Git tag (triggers optional release workflow)

```bash
git add project.yml CHANGELOG.md
git commit -m "Release v1.0.1"
git tag v1.0.1
git push && git push origin v1.0.1
```

## Before every release

- [ ] GitHub Actions **iOS Build** green on `main`
- [ ] `CHANGELOG.md` updated
- [ ] Version/build bumped in `project.yml`
- [ ] App Store metadata current (`AppStore/` folder)
- [ ] Real AdMob IDs (not Google test IDs)
- [ ] Kids compliance checklist complete

## App Store

Follow `AppStore/release-checklist.md` after tagging.

## Bundle ID guard

This app: `com.skytownstudios.cardbornheroes` — do not reuse across other apps.

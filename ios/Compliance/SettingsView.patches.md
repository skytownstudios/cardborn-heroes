# Settings patches applied by ios-kids-compliance

The apply script replaces placeholder legal URLs and wraps purchase actions with `ParentGateButton`.

If apply reports `settings_manual_patch_required`, merge manually:

1. Wrap "Remove ads", subscription buttons, and "Restore purchases" in `ParentGateButton { ... } label: { ... }`
2. Replace `https://example.com/privacy` with `KidsComplianceConfig.privacyPolicyURL`
3. Add navigation link to `KidsPrivacyManifestView` under Parents section

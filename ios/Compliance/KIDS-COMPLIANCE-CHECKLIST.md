# Kids app compliance checklist — Cardborn Heroes

Complete **before** App Store submission. This is guidance, not legal advice.

## App Store Connect

- [ ] Age rating questionnaire completed honestly
- [ ] **Kids Category** enabled only if the app qualifies (no prohibited tracking/ads)
- [ ] Privacy Policy URL set (HTTPS) → `KidsComplianceConfig.privacyPolicyURL`
- [ ] Support URL / contact email set

## Privacy

- [ ] Hosted privacy policy mentions: children's data, ads, IAP, third parties (Apple, AdMob)
- [ ] `PrivacyInfo.xcprivacy` accurate (UserDefaults reason codes, etc.)
- [ ] No behavioral ads targeted to children without compliant setup

## AdMob (if ads enabled)

- [ ] AdMob app tagged for **child-directed** treatment where required
- [ ] Test ads only in development; production units before release
- [ ] Consider: no clickable ad mascots for youngest ages (use parent-gated interstitials only)

## In-app (this skill adds)

- [ ] Parent gate on **Purchases**, **Restore**, and **external links**
- [ ] Privacy policy / terms open only after gate or from parent section
- [ ] `KidsPrivacyManifestView` reviewed for accuracy

## IAP / subscriptions

- [ ] Clear pricing text for parents
- [ ] Restore purchases available
- [ ] Subscription terms link if using auto-renewable subs

## COPPA / international

- [ ] U.S. COPPA considered if under-13 audience
- [ ] GDPR-K / regional rules if distributing in EU/UK

## Pre-launch test

- [ ] Child flow: no accidental purchase without gate
- [ ] Parent can complete gate and restore purchases
- [ ] App works with ads removed / premium active

**Last reviewed:** __________  **Reviewer:** __________

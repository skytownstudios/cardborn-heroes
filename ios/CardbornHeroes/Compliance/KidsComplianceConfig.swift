import Foundation

/// Central config for kids-app compliance. Update before App Store submission.
enum KidsComplianceConfig {
    /// 4+ , 9+ , etc. — must match App Store Connect age rating.
    static let ageRatingDescription = "Made for ages 4+"

    /// Replace with your hosted privacy policy (HTTPS required for App Store).
    static let privacyPolicyURL = "https://example.com/privacy"

    static let termsOfUseURL = "https://example.com/terms"

    static let supportEmail = "skytownstudios@gmail.com"

    /// Set true after configuring AdMob for child-directed treatment.
    static let usesChildDirectedAds = true

    /// Set true if app is in Apple Kids Category.
    static let isKidsCategoryApp = true
}

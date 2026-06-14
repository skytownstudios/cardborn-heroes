import Foundation

/// App Store Connect product identifiers — must match exactly.
enum MonetizationConfig {
    /// Non-consumable: one-time remove ads
    static let removeAdsProductID = "com.skytownstudios.cardbornheroes.remove_ads"

    /// Auto-renewable subscriptions (Premium: no ads + full access)
    static let premiumMonthlyProductID = "com.skytownstudios.cardbornheroes.premium.monthly"
    static let premiumYearlyProductID = "com.skytownstudios.cardbornheroes.premium.yearly"

    static var allProductIDs: [String] {
        [removeAdsProductID, premiumMonthlyProductID, premiumYearlyProductID]
    }
}

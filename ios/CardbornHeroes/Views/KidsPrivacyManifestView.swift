import SwiftUI

/// In-app summary of data practices for parents (not a substitute for a hosted privacy policy).
struct KidsPrivacyManifestView: View {
    var body: some View {
        List {
            Section("For parents") {
                Text(KidsComplianceConfig.ageRatingDescription)
                LabeledContent("Kids category", value: KidsComplianceConfig.isKidsCategoryApp ? "Yes" : "No")
                LabeledContent("Child-directed ads", value: KidsComplianceConfig.usesChildDirectedAds ? "Tagged" : "Review required")
            }

            Section("Data collection") {
                Text("This app uses on-device settings storage (sound preferences, purchase state).")
                Text("If ads are enabled, Google AdMob may collect data per Google's policy and your AdMob child-directed settings.")
                Text("In-app purchases are processed by Apple; we do not receive your payment card details.")
            }

            Section("Third parties") {
                Text("Apple (App Store, StoreKit)")
                Text("Google AdMob (ads, when not removed)")
            }

            Section("Your rights") {
                Text("Contact us to request data questions or account help at \(KidsComplianceConfig.supportEmail).")
            }
        }
        .navigationTitle("Privacy & data")
    }
}

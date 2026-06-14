import GoogleMobileAds
import SwiftUI

@main
struct CardbornHeroesApp: App {
    @StateObject private var purchaseService: PurchaseService
    @StateObject private var subscriptionService: SubscriptionService
    @StateObject private var premiumAccess: PremiumAccess
    @StateObject private var adService = AdService()
    @StateObject private var appSettings = AppSettings()
    @StateObject private var eduContent = EduContentStore()

    init() {
        let purchase = PurchaseService()
        let subscription = SubscriptionService()
        _purchaseService = StateObject(wrappedValue: purchase)
        _subscriptionService = StateObject(wrappedValue: subscription)
        _premiumAccess = StateObject(
            wrappedValue: PremiumAccess(
                purchaseService: purchase,
                subscriptionService: subscription
            )
        )
        GADMobileAds.sharedInstance().start(completionHandler: nil)
    }

    var body: some Scene {
        WindowGroup {
            MainTabView()
                .environmentObject(purchaseService)
                .environmentObject(subscriptionService)
                .environmentObject(premiumAccess)
                .environmentObject(adService)
                .environmentObject(appSettings)
                .environmentObject(eduContent)
                .onAppear {
                    Task {
                        await purchaseService.refreshEntitlements()
                        await subscriptionService.refreshEntitlements()
                    }
                }
                .onChange(of: premiumAccess.isPremium) { _, isPremium in
                    if isPremium {
                        adService.clearAds()
                    } else {
                        adService.loadInterstitial()
                    }
                }
        }
    }
}

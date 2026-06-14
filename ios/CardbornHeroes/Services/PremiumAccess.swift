import Combine
import Foundation

/// Single source of truth: premium = no ads (one-time OR subscription).
@MainActor
final class PremiumAccess: ObservableObject {
    let purchaseService: PurchaseService
    let subscriptionService: SubscriptionService

    private var cancellables = Set<AnyCancellable>()

    init(purchaseService: PurchaseService, subscriptionService: SubscriptionService) {
        self.purchaseService = purchaseService
        self.subscriptionService = subscriptionService
        purchaseService.objectWillChange
            .sink { [weak self] _ in self?.objectWillChange.send() }
            .store(in: &cancellables)
        subscriptionService.objectWillChange
            .sink { [weak self] _ in self?.objectWillChange.send() }
            .store(in: &cancellables)
    }

    var isPremium: Bool {
        purchaseService.adsRemoved || subscriptionService.hasActiveSubscription
    }

    var shouldShowAds: Bool { !isPremium }

    var isLoading: Bool {
        purchaseService.isLoading || subscriptionService.isLoading
    }

    var errorMessage: String? {
        purchaseService.errorMessage ?? subscriptionService.errorMessage
    }
}

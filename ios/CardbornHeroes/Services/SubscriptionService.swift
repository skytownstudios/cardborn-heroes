import Foundation
import StoreKit

@MainActor
final class SubscriptionService: ObservableObject {
    @Published private(set) var hasActiveSubscription = false
    @Published private(set) var monthlyProduct: Product?
    @Published private(set) var yearlyProduct: Product?
    @Published private(set) var isLoading = false
    @Published var errorMessage: String?

    private var listener: Task<Void, Never>?

    init() {
        hasActiveSubscription = UserDefaults.standard.bool(forKey: Self.subscriptionKey)
        listener = listenForUpdates()
        Task { await loadProducts() }
    }

    deinit { listener?.cancel() }

    private static let subscriptionKey = "hasActiveSubscription"

    func loadProducts() async {
        let ids = [
            MonetizationConfig.premiumMonthlyProductID,
            MonetizationConfig.premiumYearlyProductID,
        ]
        do {
            let products = try await Product.products(for: Set(ids))
            monthlyProduct = products.first { $0.id == MonetizationConfig.premiumMonthlyProductID }
            yearlyProduct = products.first { $0.id == MonetizationConfig.premiumYearlyProductID }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func purchaseMonthly() async { await purchase(monthlyProduct) }
    func purchaseYearly() async { await purchase(yearlyProduct) }

    private func purchase(_ product: Product?) async {
        guard let product else {
            errorMessage = "Subscription not available. Check App Store Connect."
            return
        }
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            let result = try await product.purchase()
            switch result {
            case .success(let verification):
                let transaction = try checkVerified(verification)
                await updateSubscriptionState(from: transaction)
                await transaction.finish()
            case .userCancelled:
                break
            case .pending:
                errorMessage = "Purchase pending approval."
            @unknown default:
                break
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func restorePurchases() async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            try await AppStore.sync()
            await refreshEntitlements()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func refreshEntitlements() async {
        var active = false
        let subIDs = Set([
            MonetizationConfig.premiumMonthlyProductID,
            MonetizationConfig.premiumYearlyProductID,
        ])
        for await result in Transaction.currentEntitlements {
            guard let transaction = try? checkVerified(result) else { continue }
            if subIDs.contains(transaction.productID), transaction.revocationDate == nil {
                active = true
            }
        }
        setSubscriptionActive(active)
    }

    private func listenForUpdates() -> Task<Void, Never> {
        Task.detached { [weak self] in
            for await result in Transaction.updates {
                guard let self else { continue }
                await MainActor.run {
                    guard let transaction = try? self.checkVerified(result) else { return }
                    Task {
                        await self.updateSubscriptionState(from: transaction)
                        await transaction.finish()
                    }
                }
            }
        }
    }

    private func updateSubscriptionState(from transaction: Transaction) async {
        let subIDs = [
            MonetizationConfig.premiumMonthlyProductID,
            MonetizationConfig.premiumYearlyProductID,
        ]
        guard subIDs.contains(transaction.productID) else { return }
        setSubscriptionActive(transaction.revocationDate == nil)
    }

    private func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .unverified: throw StoreError.failedVerification
        case .verified(let safe): return safe
        }
    }

    private func setSubscriptionActive(_ value: Bool) {
        hasActiveSubscription = value
        UserDefaults.standard.set(value, forKey: Self.subscriptionKey)
    }
}

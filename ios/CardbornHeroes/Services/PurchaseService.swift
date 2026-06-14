import Foundation
import StoreKit

enum StoreError: Error {
    case failedVerification
}

@MainActor
final class PurchaseService: ObservableObject {
    static let removeAdsProductID = MonetizationConfig.removeAdsProductID

    @Published private(set) var adsRemoved = false
    @Published private(set) var removeAdsProduct: Product?
    @Published private(set) var isLoading = false
    @Published var errorMessage: String?

    private var transactionListener: Task<Void, Never>?

    init() {
        adsRemoved = UserDefaults.standard.bool(forKey: Self.adsRemovedKey)
        transactionListener = listenForTransactions()
        Task { await loadProducts() }
    }

    deinit {
        transactionListener?.cancel()
    }

    private static let adsRemovedKey = "adsRemoved"

    func loadProducts() async {
        do {
            let products = try await Product.products(for: [Self.removeAdsProductID])
            removeAdsProduct = products.first
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func purchaseRemoveAds() async {
        guard let product = removeAdsProduct else {
            errorMessage = "Remove ads is not available yet. Check App Store Connect."
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
                await updateAdsRemoved(from: transaction)
                await transaction.finish()
            case .userCancelled:
                break
            case .pending:
                errorMessage = "Purchase is pending approval."
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
            if !adsRemoved {
                errorMessage = "No previous purchases found for this Apple ID."
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func refreshEntitlements() async {
        var hasRemoveAds = false
        for await result in Transaction.currentEntitlements {
            guard let transaction = try? checkVerified(result) else { continue }
            if transaction.productID == Self.removeAdsProductID, transaction.revocationDate == nil {
                hasRemoveAds = true
            }
        }
        setAdsRemoved(hasRemoveAds)
    }

    private func listenForTransactions() -> Task<Void, Never> {
        Task.detached { [weak self] in
            for await result in Transaction.updates {
                guard let self else { continue }
                await MainActor.run {
                    guard let transaction = try? self.checkVerified(result) else { return }
                    Task {
                        await self.updateAdsRemoved(from: transaction)
                        await transaction.finish()
                    }
                }
            }
        }
    }

    private func updateAdsRemoved(from transaction: Transaction) async {
        if transaction.productID == Self.removeAdsProductID {
            setAdsRemoved(transaction.revocationDate == nil)
        }
    }

    private func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .unverified:
            throw StoreError.failedVerification
        case .verified(let safe):
            return safe
        }
    }

    private func setAdsRemoved(_ value: Bool) {
        adsRemoved = value
        UserDefaults.standard.set(value, forKey: Self.adsRemovedKey)
    }
}

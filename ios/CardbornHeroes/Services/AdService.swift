import GoogleMobileAds
import UIKit

@MainActor
final class AdService: NSObject, ObservableObject {
    @Published private(set) var isInterstitialReady = false

    private var interstitial: GADInterstitialAd?
    private let interstitialAdUnitID: String

    override init() {
        interstitialAdUnitID = "ca-app-pub-3940256099942544/4411468910"
        super.init()
    }

    func loadInterstitial() {
        GADInterstitialAd.load(
            withAdUnitID: interstitialAdUnitID,
            request: GADRequest()
        ) { [weak self] ad, error in
            Task { @MainActor in
                guard let self else { return }
                if error != nil {
                    self.interstitial = nil
                    self.isInterstitialReady = false
                    return
                }
                self.interstitial = ad
                self.isInterstitialReady = ad != nil
            }
        }
    }

    func showInterstitialIfReady() {
        guard let interstitial,
              let root = UIApplication.shared.topViewController else {
            loadInterstitial()
            return
        }
        interstitial.present(fromRootViewController: root)
        self.interstitial = nil
        isInterstitialReady = false
        loadInterstitial()
    }

    func clearAds() {
        interstitial = nil
        isInterstitialReady = false
    }
}

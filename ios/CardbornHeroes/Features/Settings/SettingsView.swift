import StoreKit
import SwiftUI

struct SettingsView: View {
    @EnvironmentObject private var purchaseService: PurchaseService
    @EnvironmentObject private var subscriptionService: SubscriptionService
    @EnvironmentObject private var premiumAccess: PremiumAccess
    @EnvironmentObject private var appSettings: AppSettings

    var body: some View {
        NavigationStack {
            Form {
                Section("Audio") {
                    Toggle("Sound effects", isOn: $appSettings.soundEffectsEnabled)
                    Toggle("Music", isOn: $appSettings.musicEnabled)
                }

                if premiumAccess.isPremium {
                    Section("Premium") {
                        Label("Premium active", systemImage: "checkmark.seal.fill")
                            .foregroundStyle(.green)
                        if purchaseService.adsRemoved {
                            Text("Ad removal (lifetime)").font(.footnote).foregroundStyle(.secondary)
                        }
                        if subscriptionService.hasActiveSubscription {
                            Text("Subscription active").font(.footnote).foregroundStyle(.secondary)
                        }
                    }
                } else {
                    Section("Go Premium") {
                        ParentGateButton(gateTitle: "Parents — monthly") {
                            Task { await subscriptionService.purchaseMonthly() }
                        } label: {
                            priceRow("Monthly", subscriptionService.monthlyProduct)
                        }

                        ParentGateButton(gateTitle: "Parents — yearly") {
                            Task { await subscriptionService.purchaseYearly() }
                        } label: {
                            priceRow("Yearly (best value)", subscriptionService.yearlyProduct)
                        }

                        ParentGateButton(gateTitle: "Parents — remove ads") {
                            Task { await purchaseService.purchaseRemoveAds() }
                        } label: {
                            HStack {
                                Text("Remove ads only (one-time)")
                                Spacer()
                                if let p = purchaseService.removeAdsProduct {
                                    Text(p.displayPrice).foregroundStyle(.secondary)
                                }
                            }
                        }
                    }
                }

                Section {
                    ParentGateButton(gateTitle: "Parents — restore") {
                        Task {
                            await purchaseService.restorePurchases()
                            await subscriptionService.restorePurchases()
                        }
                    } label: {
                        Text("Restore purchases")
                    }
                    .disabled(premiumAccess.isLoading)
                }

                parentsSection
                aboutSection

                if let error = premiumAccess.errorMessage {
                    Section {
                        Text(error).foregroundStyle(.red).font(.footnote)
                    }
                }
            }
            .navigationTitle("Settings")
            .overlay {
                if premiumAccess.isLoading {
                    ProgressView()
                        .padding()
                        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 12))
                }
            }
        }
    }

    @ViewBuilder
    private var parentsSection: some View {
        Section("Parents") {
            NavigationLink("Privacy & data") { KidsPrivacyManifestView() }
            ParentGateButton(gateTitle: "Parents only") { openURL(KidsComplianceConfig.privacyPolicyURL) } label: {
                Text("Privacy policy (web)")
            }
            ParentGateButton(gateTitle: "Parents only") { openURL(KidsComplianceConfig.termsOfUseURL) } label: {
                Text("Terms of use")
            }
            ParentGateButton(gateTitle: "Parents only") {
                openURL("mailto:\(KidsComplianceConfig.supportEmail)")
            } label: {
                Text("Contact support")
            }
        }
    }

    @ViewBuilder
    private var aboutSection: some View {
        Section("About") {
            LabeledContent("Version", value: Bundle.main.appVersionString)
        }
    }

    private func priceRow(_ title: String, _ product: Product?) -> some View {
        HStack {
            Text(title)
            Spacer()
            if let product { Text(product.displayPrice).foregroundStyle(.secondary) }
        }
    }

    private func openURL(_ string: String) {
        guard let url = URL(string: string) else { return }
        UIApplication.shared.open(url)
    }
}

private extension Bundle {
    var appVersionString: String {
        let version = infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
        let build = infoDictionary?["CFBundleVersion"] as? String ?? "1"
        return "\(version) (\(build))"
    }
}

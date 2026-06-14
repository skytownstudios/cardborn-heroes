import SwiftUI

struct MainTabView: View {
    @EnvironmentObject private var purchaseService: PurchaseService

    var body: some View {
        TabView {
            HomeView()
                .tabItem {
                    Label("Play", systemImage: "gamecontroller.fill")
                }

            SettingsView()
                .tabItem {
                    Label("Settings", systemImage: "gearshape.fill")
                }
        }
    }
}

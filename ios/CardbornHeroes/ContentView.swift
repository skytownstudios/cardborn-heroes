import SwiftUI

struct ContentView: View {
    @State private var tab = 0
    private let tabs = ["Battle", "Hand", "Cards", "Forge", "Shop"]

    var body: some View {
        TabView(selection: $tab) {
            ForEach(0..<tabs.count, id: \.self) { i in
                VStack(spacing: 12) {
                    Text("Cardborn Heroes")
                        .font(.title2.bold())
                        .foregroundStyle(GildedIvory.textCharcoal)
                    Text(tabs[i])
                        .foregroundStyle(GildedIvory.textMuted)
                    Text("iOS parity shell — gameplay on Android v1")
                        .font(.caption)
                        .foregroundStyle(GildedIvory.textMuted)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(GildedIvory.backgroundCream)
                .tabItem { Text(tabs[i]) }
                .tag(i)
            }
        }
        .tint(GildedIvory.primaryGold)
    }
}

#Preview {
    ContentView()
}

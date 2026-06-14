import SwiftUI

struct HomeView: View {
    @EnvironmentObject private var appSettings: AppSettings

    var body: some View {
        NavigationStack {
            MatchingGameView()
                .navigationTitle("Cardborn Heroes")
                .navigationBarTitleDisplayMode(.inline)
        }
    }
}

/// Replace this view with your game (farmer, animals, etc.).
struct GamePlaceholderView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "leaf.fill")
                .font(.system(size: 64))
                .foregroundStyle(.green)

            Text("Game goes here")
                .font(.title2.bold())

            Text("Build your farm walkthrough, animal taps, and sounds in this view.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(
            LinearGradient(
                colors: [Color(red: 0.85, green: 0.95, blue: 0.8), .white],
                startPoint: .top,
                endPoint: .bottom
            )
        )
    }
}

#Preview {
    HomeView()
        .environmentObject(PurchaseService())
        .environmentObject(AdService())
        .environmentObject(AppSettings())
}

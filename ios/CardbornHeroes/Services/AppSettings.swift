import SwiftUI

@MainActor
final class AppSettings: ObservableObject {
    @AppStorage("soundEffectsEnabled") var soundEffectsEnabled = true
    @AppStorage("musicEnabled") var musicEnabled = true
    @AppStorage("hasCompletedOnboarding") var hasCompletedOnboarding = false
}

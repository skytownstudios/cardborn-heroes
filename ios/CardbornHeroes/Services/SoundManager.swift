import AVFoundation
import Foundation

@MainActor
final class SoundManager {
    static let shared = SoundManager()

    private var players: [String: AVAudioPlayer] = [:]

    private init() {
        try? AVAudioSession.sharedInstance().setCategory(.ambient, mode: .default)
        try? AVAudioSession.sharedInstance().setActive(true)
    }

    func play(named name: String, ext: String = "m4a") {
        let key = "\(name).\(ext)"
        if let player = players[key], player.isPlaying {
            player.currentTime = 0
            player.play()
            return
        }
        guard let url = Bundle.main.url(forResource: name, withExtension: ext) else {
            // Fallback: try mp3
            if ext != "mp3", let url = Bundle.main.url(forResource: name, withExtension: "mp3") {
                playURL(url, key: "\(name).mp3")
            }
            return
        }
        playURL(url, key: key)
    }

    private func playURL(_ url: URL, key: String) {
        do {
            let player = try AVAudioPlayer(contentsOf: url)
            player.prepareToPlay()
            players[key] = player
            player.play()
        } catch {
            // Missing audio file — silent in production
        }
    }
}

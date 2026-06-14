import SwiftUI

struct MatchingPair: Codable, Identifiable {
    let id: String
    let emoji: String
    let label: String
}

struct MatchingContent: Codable {
    let title: String
    let pairs: [MatchingPair]
}

struct MatchingGameView: View {
    @State private var content: MatchingContent?
    @State private var cards: [Card] = []
    @State private var flipped: [Int] = []
    @State private var matched: Set<Int> = []

    struct Card: Identifiable {
        let id: Int
        let pairID: String
        let display: String
    }

    var body: some View {
        VStack(spacing: 16) {
            Text(content?.title ?? "Matching").font(.title2.bold())
            LazyVGrid(columns: [GridItem(.adaptive(minimum: 72))], spacing: 12) {
                ForEach(cards) { card in
                    let isUp = flipped.contains(card.id) || matched.contains(card.id)
                    Button {
                        tap(card)
                    } label: {
                        ZStack {
                            RoundedRectangle(cornerRadius: 12)
                                .fill(isUp ? Color.accentColor.opacity(0.2) : Color.gray.opacity(0.3))
                                .frame(height: 72)
                            Text(isUp ? card.display : "?")
                                .font(.largeTitle)
                        }
                    }
                    .disabled(matched.contains(card.id) || flipped.count == 2 && !flipped.contains(card.id))
                }
            }
            .padding()
        }
        .onAppear { load() }
    }

    private func load() {
        guard let url = Bundle.main.url(forResource: "matching", withExtension: "json"),
              let data = try? Data(contentsOf: url),
              let decoded = try? JSONDecoder().decode(MatchingContent.self, from: data) else { return }
        content = decoded
        var built: [Card] = []
        var i = 0
        for p in decoded.pairs {
            built.append(Card(id: i, pairID: p.id, display: p.emoji))
            i += 1
            built.append(Card(id: i, pairID: p.id, display: p.label))
            i += 1
        }
        cards = built.shuffled()
    }

    private func tap(_ card: Card) {
        guard !flipped.contains(card.id), !matched.contains(card.id) else { return }
        flipped.append(card.id)
        if flipped.count == 2 {
            let a = cards[flipped[0]]
            let b = cards[flipped[1]]
            if a.pairID == b.pairID {
                matched.insert(flipped[0])
                matched.insert(flipped[1])
                flipped.removeAll()
            } else {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) {
                    flipped.removeAll()
                }
            }
        }
    }
}

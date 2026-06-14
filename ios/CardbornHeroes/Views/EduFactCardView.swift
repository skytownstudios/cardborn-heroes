import SwiftUI

/// Reusable fact card for any educational game.
struct EduFactCardView: View {
    let lesson: LessonItem
    let buttonTitle: String
    let onContinue: () -> Void

    init(lesson: LessonItem, buttonTitle: String = "Next", onContinue: @escaping () -> Void) {
        self.lesson = lesson
        self.buttonTitle = buttonTitle
        self.onContinue = onContinue
    }

    var body: some View {
        VStack(spacing: 16) {
            Text(lesson.symbol)
                .font(.system(size: 56))
            Text(lesson.title)
                .font(.title.bold())
            Text(lesson.fact)
                .font(.body)
                .multilineTextAlignment(.center)
                .foregroundStyle(.secondary)
                .padding(.horizontal)
            Button(buttonTitle, action: onContinue)
                .buttonStyle(.borderedProminent)
        }
        .padding(24)
        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 20))
        .padding(32)
        .accessibilityElement(children: .combine)
    }
}

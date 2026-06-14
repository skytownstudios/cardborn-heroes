import SwiftUI

/// Simple parental gate (math challenge) before purchases, external links, or ad settings.
struct ParentGateView: View {
    @Environment(\.dismiss) private var dismiss

    let title: String
    let onSuccess: () -> Void

    @State private var answerText = ""
    @State private var showError = false

    private let operandA: Int
    private let operandB: Int

    init(title: String = "Parents only", onSuccess: @escaping () -> Void) {
        self.title = title
        self.onSuccess = onSuccess
        operandA = Int.random(in: 11...19)
        operandB = Int.random(in: 2...9)
    }

    private var correctAnswer: Int { operandA + operandB }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Text("This area is for parents and guardians.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }

                Section("Solve to continue") {
                    Text("What is \(operandA) + \(operandB)?")
                        .font(.title3.bold())
                    TextField("Answer", text: $answerText)
                        .keyboardType(.numberPad)
                }

                if showError {
                    Section {
                        Text("Incorrect answer. Please try again.")
                            .foregroundStyle(.red)
                            .font(.footnote)
                    }
                }
            }
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Continue") { submit() }
                        .fontWeight(.semibold)
                }
            }
        }
    }

    private func submit() {
        guard Int(answerText.trimmingCharacters(in: .whitespaces)) == correctAnswer else {
            showError = true
            return
        }
        dismiss()
        onSuccess()
    }
}

import SwiftUI

/// Presents `ParentGateView` before running a sensitive action.
struct ParentGateButton<Label: View>: View {
    let gateTitle: String
    let action: () -> Void
    @ViewBuilder let label: () -> Label

    @State private var showGate = false

    init(
        gateTitle: String = "Parents only",
        action: @escaping () -> Void,
        @ViewBuilder label: @escaping () -> Label
    ) {
        self.gateTitle = gateTitle
        self.action = action
        self.label = label
    }

    var body: some View {
        Button {
            showGate = true
        } label: {
            label()
        }
        .sheet(isPresented: $showGate) {
            ParentGateView(title: gateTitle, onSuccess: action)
        }
    }
}

extension View {
    func parentGatedSheet(
        isPresented: Binding<Bool>,
        title: String = "Parents only",
        onSuccess: @escaping () -> Void
    ) -> some View {
        sheet(isPresented: isPresented) {
            ParentGateView(title: title, onSuccess: {
                isPresented.wrappedValue = false
                onSuccess()
            })
        }
    }
}

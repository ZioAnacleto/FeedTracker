import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @ObservedObject private var statusBar = StatusBarStyleModel.shared

    var body: some View {
        ComposeView()
            .ignoresSafeArea()
            .preferredColorScheme(statusBar.colorScheme)
    }
}




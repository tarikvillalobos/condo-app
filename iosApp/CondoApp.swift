import SwiftUI
import CondoApp

@main
struct CondoApplication: App {
    var body: some Scene {
        WindowGroup {
            ResidentView().ignoresSafeArea()
        }
    }
}

struct ResidentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ controller: UIViewController, context: Context) {}
}

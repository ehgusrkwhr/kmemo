import SwiftUI
import WidgetKit
import ComposeApp

@main
struct iOSApp: App {
    init() {
        // Let Kotlin trigger a widget refresh whenever memos change.
        IosWidgetReloader.shared.reload = {
            WidgetCenter.shared.reloadAllTimelines()
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all)
                .onOpenURL { url in
                    // kmemo://memo/{id} — open that memo in the shared Compose UI.
                    if url.scheme == "kmemo", url.host == "memo",
                       let id = Int64(url.lastPathComponent) {
                        DeepLink.shared.requestOpen(id: id)
                    }
                }
        }
    }
}

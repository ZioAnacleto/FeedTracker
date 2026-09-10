import SwiftUI
import WidgetKit
import ComposeApp

@main
struct iOSApp: App {
    init() {
        KoinInitIosKt.doInitKoinIos()
        TrackingSessionLivePresenter.configure()
        IosActiveTrackingSessionStoreKt.setIosWidgetReloader {
            TrackingSessionLivePresenter.sync()
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    if url.scheme == "feedtracker" {
                        NewTrackingNavigator.shared.requestOpen()
                    }
                }
        }
    }
}

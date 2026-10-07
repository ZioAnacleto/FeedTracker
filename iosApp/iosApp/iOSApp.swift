import SwiftUI
import WidgetKit
import ComposeApp

final class StatusBarStyleModel: ObservableObject {
    static let shared = StatusBarStyleModel()
    @Published var colorScheme: ColorScheme?
}

@main
struct iOSApp: App {
    init() {
        KoinInitIosKt.doInitKoinIos()
        TrackingSessionLivePresenter.configure()
        IosActiveTrackingSessionStoreKt.setIosWidgetReloader {
            TrackingSessionLivePresenter.sync()
        }
        LocalAppLocale_iosKt.applyStoredIosLanguage()
        IosStatusBarKt.setIosStatusBarStyle { followSystem, darkTheme in
            let follow = followSystem.boolValue
            let dark = darkTheme.boolValue
            DispatchQueue.main.async {
                StatusBarStyleModel.shared.colorScheme = follow ? nil : (dark ? .dark : .light)
            }
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

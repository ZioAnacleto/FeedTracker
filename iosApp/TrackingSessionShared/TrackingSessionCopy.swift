import Foundation

enum TrackingSessionCopy {
    static var tracking: String {
        resolved(key: "widget_tracking", english: "Tracking", italian: "In corso")
    }

    static var tapToStart: String {
        resolved(key: "widget_idle", english: "Tap to start", italian: "Tocca per iniziare")
    }

    private static func resolved(key: String, english: String, italian: String) -> String {
        switch UserDefaults(suiteName: TrackingSessionPresentationDefaults.appGroupId)?
            .string(forKey: "app_language") {
        case "en":
            return english
        case "it":
            return italian
        default:
            return NSLocalizedString(key, comment: "")
        }
    }
}

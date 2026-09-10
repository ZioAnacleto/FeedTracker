import ActivityKit
import Foundation
import SwiftUI

struct TrackingSessionAttributes: ActivityAttributes {
    struct ContentState: Codable, Hashable {
        var startTimeMillis: Int64
        var name: String = ""
        var surname: String = ""

        var startDate: Date {
            Date(timeIntervalSince1970: TimeInterval(startTimeMillis) / 1000)
        }

        var displayName: String {
            [name, surname]
                .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
                .filter { !$0.isEmpty }
                .joined(separator: " ")
        }

        var compactLabel: String {
            let initials = [name, surname]
                .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
                .compactMap { $0.first }
                .map { String($0).uppercased() }
            if initials.isEmpty { return "FT" }
            return initials.prefix(2).joined()
        }
    }
}

enum TrackingSessionPresentationDefaults {
    static let appGroupId = "group.com.zioanacleto.feedtracker"
    static let startTimeKey = "start_time_millis"
    static let nameKey = "name"
    static let surnameKey = "surname"
    static let widgetKind = "TrackingSessionWidget"
    static let notificationId = "tracking-session-timer"
    static let newTrackingURL = URL(string: "feedtracker://new-tracking")!
}

enum FeedTrackerSunsetStyle {
    static let vermilion = Color(red: 94 / 255, green: 11 / 255, blue: 11 / 255)
    static let brightOrange = Color(red: 251 / 255, green: 140 / 255, blue: 0 / 255)

    static var gradient: LinearGradient {
        LinearGradient(
            colors: [vermilion, brightOrange],
            startPoint: .top,
            endPoint: .bottom
        )
    }
}

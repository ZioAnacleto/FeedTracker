import SwiftUI
import WidgetKit

enum TrackingSessionWidgetDefaults {
    static let appGroupId = "group.com.zioanacleto.feedtracker"
    static let startTimeKey = "start_time_millis"
    static let kind = "TrackingSessionWidget"
    static let newTrackingURL = URL(string: "feedtracker://new-tracking")!
}

struct TrackingSessionEntry: TimelineEntry {
    let date: Date
    let startDate: Date?
}

struct TrackingSessionProvider: TimelineProvider {
    func placeholder(in context: Context) -> TrackingSessionEntry {
        TrackingSessionEntry(date: Date(), startDate: Date().addingTimeInterval(-95))
    }

    func getSnapshot(in context: Context, completion: @escaping (TrackingSessionEntry) -> Void) {
        completion(currentEntry())
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<TrackingSessionEntry>) -> Void) {
        let timeline = Timeline(entries: [currentEntry()], policy: .never)
        completion(timeline)
    }

    private func currentEntry() -> TrackingSessionEntry {
        let startMillis = UserDefaults(suiteName: TrackingSessionWidgetDefaults.appGroupId)?
            .object(forKey: TrackingSessionWidgetDefaults.startTimeKey) as? Int64
        let startDate = startMillis.map { Date(timeIntervalSince1970: TimeInterval($0) / 1000.0) }
        return TrackingSessionEntry(date: Date(), startDate: startDate)
    }
}

struct TrackingSessionWidgetView: View {
    var entry: TrackingSessionEntry

    var body: some View {
        VStack(spacing: 6) {
            Text(entry.startDate == nil ? "Tap to start" : "Tracking")
                .font(.subheadline.weight(.medium))
                .foregroundStyle(.white.opacity(0.9))
            if let startDate = entry.startDate {
                Text(startDate, style: .timer)
                    .font(.system(size: 28, weight: .bold, design: .rounded))
                    .monospacedDigit()
                    .foregroundStyle(.white)
                    .minimumScaleFactor(0.6)
                    .lineLimit(1)
            } else {
                Text("--:--")
                    .font(.system(size: 28, weight: .bold, design: .rounded))
                    .monospacedDigit()
                    .foregroundStyle(.white)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .containerBackground(for: .widget) {
            LinearGradient(
                colors: [
                    FeedTrackerSunsetStyle.vermilion,
                    FeedTrackerSunsetStyle.brightOrange,
                ],
                startPoint: .top,
                endPoint: .bottom
            )
        }
    }
}

struct TrackingSessionWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(
            kind: TrackingSessionWidgetDefaults.kind,
            provider: TrackingSessionProvider()
        ) { entry in
            TrackingSessionWidgetView(entry: entry)
                .widgetURL(TrackingSessionWidgetDefaults.newTrackingURL)
        }
        .configurationDisplayName("FeedTracker timer")
        .description("Shows the live timer for the active tracking session.")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

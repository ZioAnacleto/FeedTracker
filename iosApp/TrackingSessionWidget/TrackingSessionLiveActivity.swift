import ActivityKit
import SwiftUI
import WidgetKit

struct TrackingSessionLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: TrackingSessionAttributes.self) { context in
            TrackingSessionLiveActivityLockScreenView(
                startDate: context.state.startDate,
                displayName: context.state.displayName
            )
            .widgetURL(TrackingSessionPresentationDefaults.newTrackingURL)
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.bottom) {
                    TrackingSessionExpandedIslandView(
                        startDate: context.state.startDate,
                        displayName: context.state.displayName
                    )
                }
            } compactLeading: {
                Text(context.state.compactLabel)
                    .font(.caption.weight(.bold))
                    .foregroundStyle(.white)
            } compactTrailing: {
                Text(context.state.startDate, style: .timer)
                    .font(.caption.monospacedDigit().weight(.semibold))
                    .foregroundStyle(.white)
                    .minimumScaleFactor(0.5)
                    .frame(maxWidth: 52)
            } minimal: {
                Text(context.state.startDate, style: .timer)
                    .font(.caption2.monospacedDigit())
                    .foregroundStyle(.white)
                    .minimumScaleFactor(0.5)
            }
            .keylineTint(FeedTrackerSunsetStyle.brightOrange)
            .widgetURL(TrackingSessionPresentationDefaults.newTrackingURL)
        }
    }
}

private struct TrackingSessionExpandedIslandView: View {
    let startDate: Date
    let displayName: String

    var body: some View {
        VStack(spacing: 6) {
            Text("Tracking")
                .font(.subheadline.weight(.medium))
                .foregroundStyle(.white.opacity(0.9))
            if !displayName.isEmpty {
                Text(displayName)
                    .font(.headline.weight(.semibold))
                    .foregroundStyle(.white)
                    .lineLimit(1)
                    .minimumScaleFactor(0.7)
            }
            Text(startDate, style: .timer)
                .font(.system(size: 32, weight: .bold, design: .rounded))
                .monospacedDigit()
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 12)
        .padding(.top, 2)
        .padding(.bottom, 10)
        .background {
            FeedTrackerSunsetStyle.gradient
                .padding(.horizontal, -24)
                .padding(.vertical, -20)
        }
    }
}

private struct TrackingSessionLiveActivityLockScreenView: View {
    let startDate: Date
    let displayName: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("Tracking")
                .font(.subheadline.weight(.medium))
                .foregroundStyle(.white.opacity(0.9))
            if !displayName.isEmpty {
                Text(displayName)
                    .font(.headline.weight(.semibold))
                    .foregroundStyle(.white)
                    .lineLimit(1)
            }
            Text(startDate, style: .timer)
                .font(.system(size: 28, weight: .bold, design: .rounded))
                .monospacedDigit()
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .containerBackground(for: .widget) {
            FeedTrackerSunsetStyle.gradient
        }
    }
}

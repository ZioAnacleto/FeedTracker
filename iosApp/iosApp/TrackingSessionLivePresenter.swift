import ActivityKit
import ComposeApp
import Foundation
import UIKit
import UserNotifications
import WidgetKit

enum TrackingSessionLivePresenter {
    private static let notificationDelegate = TrackingSessionNotificationDelegate()
    private static var notificationTicker: Task<Void, Never>?

    static func configure() {
        UNUserNotificationCenter.current().delegate = notificationDelegate
    }

    static func sync() {
        WidgetCenter.shared.reloadTimelines(ofKind: TrackingSessionPresentationDefaults.widgetKind)
        let startMillis = UserDefaults(suiteName: TrackingSessionPresentationDefaults.appGroupId)?
            .object(forKey: TrackingSessionPresentationDefaults.startTimeKey) as? Int64
        Task { @MainActor in
            if let startMillis {
                await present(startTimeMillis: startMillis, person: currentPerson())
            } else {
                await dismiss()
            }
        }
    }

    @MainActor
    private static func present(startTimeMillis: Int64, person: (name: String, surname: String)) async {
        if shouldUseLiveActivity {
            await endNotificationTicker()
            await startOrUpdateLiveActivity(startTimeMillis: startTimeMillis, person: person)
        } else {
            await endLiveActivities()
            await startNotificationTicker(startTimeMillis: startTimeMillis, person: person)
        }
    }

    @MainActor
    private static func dismiss() async {
        await endNotificationTicker()
        await endLiveActivities()
        UNUserNotificationCenter.current()
            .removeDeliveredNotifications(withIdentifiers: [TrackingSessionPresentationDefaults.notificationId])
        UNUserNotificationCenter.current()
            .removePendingNotificationRequests(withIdentifiers: [TrackingSessionPresentationDefaults.notificationId])
    }

    private static var shouldUseLiveActivity: Bool {
        guard #available(iOS 16.1, *) else { return false }
        guard hasDynamicIsland else { return false }
        return ActivityAuthorizationInfo().areActivitiesEnabled
    }

    private static var hasDynamicIsland: Bool {
        guard UIDevice.current.userInterfaceIdiom == .phone else { return false }
        let topInset = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first(where: \.isKeyWindow)?
            .safeAreaInsets.top ?? 0
        return topInset >= 51
    }

    @MainActor
    private static func startOrUpdateLiveActivity(
        startTimeMillis: Int64,
        person: (name: String, surname: String)
    ) async {
        guard #available(iOS 16.1, *) else { return }
        let state = TrackingSessionAttributes.ContentState(
            startTimeMillis: startTimeMillis,
            name: person.name,
            surname: person.surname
        )
        let content = ActivityContent(state: state, staleDate: nil)
        if let existing = Activity<TrackingSessionAttributes>.activities.first {
            await existing.update(content)
            return
        }
        do {
            _ = try Activity.request(
                attributes: TrackingSessionAttributes(),
                content: content,
                pushType: nil
            )
        } catch {
            await startNotificationTicker(startTimeMillis: startTimeMillis, person: person)
        }
    }

    @MainActor
    private static func endLiveActivities() async {
        guard #available(iOS 16.1, *) else { return }
        for activity in Activity<TrackingSessionAttributes>.activities {
            await activity.end(nil, dismissalPolicy: .immediate)
        }
    }

    @MainActor
    private static func startNotificationTicker(
        startTimeMillis: Int64,
        person: (name: String, surname: String)
    ) async {
        let center = UNUserNotificationCenter.current()
        let granted = await requestNotificationAuthorization(center)
        guard granted else { return }
        notificationTicker?.cancel()
        notificationTicker = Task { @MainActor in
            while !Task.isCancelled {
                await postTimerNotification(startTimeMillis: startTimeMillis, person: person)
                try? await Task.sleep(nanoseconds: 1_000_000_000)
            }
        }
    }

    @MainActor
    private static func endNotificationTicker() async {
        notificationTicker?.cancel()
        notificationTicker = nil
        UNUserNotificationCenter.current()
            .removeDeliveredNotifications(withIdentifiers: [TrackingSessionPresentationDefaults.notificationId])
        UNUserNotificationCenter.current()
            .removePendingNotificationRequests(withIdentifiers: [TrackingSessionPresentationDefaults.notificationId])
    }

    private static func requestNotificationAuthorization(_ center: UNUserNotificationCenter) async -> Bool {
        let settings = await center.notificationSettings()
        switch settings.authorizationStatus {
        case .authorized, .provisional, .ephemeral:
            return true
        case .denied:
            return false
        case .notDetermined:
            return (try? await center.requestAuthorization(options: [.alert, .badge])) ?? false
        @unknown default:
            return false
        }
    }

    private static func postTimerNotification(
        startTimeMillis: Int64,
        person: (name: String, surname: String)
    ) async {
        let content = UNMutableNotificationContent()
        let displayName = [person.name, person.surname]
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }
            .joined(separator: " ")
        content.title = displayName.isEmpty ? "Tracking" : displayName
        content.body = elapsedTimerText(since: startTimeMillis)
        content.sound = nil
        content.interruptionLevel = .passive
        let request = UNNotificationRequest(
            identifier: TrackingSessionPresentationDefaults.notificationId,
            content: content,
            trigger: nil
        )
        try? await UNUserNotificationCenter.current().add(request)
    }

    private static func currentPerson() -> (name: String, surname: String) {
        let defaults = UserDefaults(suiteName: TrackingSessionPresentationDefaults.appGroupId)
        return (
            name: defaults?.string(forKey: TrackingSessionPresentationDefaults.nameKey) ?? "",
            surname: defaults?.string(forKey: TrackingSessionPresentationDefaults.surnameKey) ?? ""
        )
    }

    private static func elapsedTimerText(since startTimeMillis: Int64) -> String {
        let elapsed = max(0, Int(Date().timeIntervalSince1970 - TimeInterval(startTimeMillis) / 1000))
        let hours = elapsed / 3600
        let minutes = (elapsed % 3600) / 60
        let seconds = elapsed % 60
        if hours > 0 {
            return String(format: "%02d:%02d:%02d", hours, minutes, seconds)
        }
        return String(format: "%02d:%02d", minutes, seconds)
    }
}

final class TrackingSessionNotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.list])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        NewTrackingNavigator.shared.requestOpen()
        completionHandler()
    }
}

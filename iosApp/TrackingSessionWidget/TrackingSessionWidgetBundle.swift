import SwiftUI
import WidgetKit

@main
struct TrackingSessionWidgetBundle: WidgetBundle {
    var body: some Widget {
        TrackingSessionWidget()
        TrackingSessionLiveActivity()
    }
}

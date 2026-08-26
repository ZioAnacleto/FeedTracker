package com.zioanacleto.feedtracker.features.home

import com.zioanacleto.feedtracker.domain.TrackingSessionModel

sealed interface HomeSessionListItem {
    data class Single(val session: TrackingSessionModel) : HomeSessionListItem

    data class Group(
        val key: String,
        val name: String,
        val surname: String,
        val birthDate: String,
        val sessions: List<TrackingSessionModel>,
        val isExpanded: Boolean,
    ) : HomeSessionListItem
}

internal fun personGroupingKey(session: TrackingSessionModel): String =
    "${session.name.trim().lowercase()}\u0000${session.surname.trim().lowercase()}"

internal fun buildHomeSessionListItems(
    sessions: List<TrackingSessionModel>,
    expandedGroupKeys: Set<String> = emptySet(),
): List<HomeSessionListItem> {
    val newestFirst = compareByDescending<TrackingSessionModel> { it.sessionStartTime }
        .thenByDescending { it.sessionEndTime }
        .thenBy { it.id }

    return sessions
        .groupBy(::personGroupingKey)
        .map { (key, groupedSessions) ->
            val sortedSessions = groupedSessions.sortedWith(newestFirst)
            val latest = sortedSessions.first()
            if (sortedSessions.size == 1) {
                HomeSessionListItem.Single(latest)
            } else {
                HomeSessionListItem.Group(
                    key = key,
                    name = latest.name,
                    surname = latest.surname,
                    birthDate = latest.birthDate,
                    sessions = sortedSessions,
                    isExpanded = key in expandedGroupKeys,
                )
            }
        }
        .sortedWith(
            compareByDescending<HomeSessionListItem> { item ->
                when (item) {
                    is HomeSessionListItem.Single -> item.session.sessionStartTime
                    is HomeSessionListItem.Group -> item.sessions.first().sessionStartTime
                }
            }.thenBy { item ->
                when (item) {
                    is HomeSessionListItem.Single -> item.session.id
                    is HomeSessionListItem.Group -> item.key
                }
            },
        )
}

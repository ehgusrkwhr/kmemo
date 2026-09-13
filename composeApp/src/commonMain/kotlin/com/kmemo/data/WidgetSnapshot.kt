package com.kmemo.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Publishes the current memos to a location the home-screen widget can read
 * (App Group UserDefaults on iOS, SharedPreferences on Android) and asks the
 * platform to refresh its widgets.
 */
expect class WidgetSnapshotPublisher {
    fun publish(json: String)
}

object WidgetSnapshot {
    /** Shared key/suite name used on both platforms. */
    const val KEY = "kmemo_widget_memos"
    const val APP_GROUP = "group.com.kmemo.app"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true // keep every field so the iOS widget can decode reliably
    }

    /** Serialize the memos the widget should show (pinned first, capped). */
    fun encode(memos: List<Memo>, limit: Int = 8): String =
        json.encodeToString(memos.take(limit))
}

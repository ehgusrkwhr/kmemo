package com.kmemo.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.kmemo.db.KmemoDatabase
import platform.Foundation.NSDate
import platform.Foundation.NSUserDefaults
import platform.Foundation.timeIntervalSince1970

actual fun nowMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver =
        NativeSqliteDriver(KmemoDatabase.Schema, "kmemo.db")
}

actual class WidgetSnapshotPublisher {
    actual fun publish(json: String) {
        val defaults = NSUserDefaults(suiteName = WidgetSnapshot.APP_GROUP)
        defaults.setObject(json, forKey = WidgetSnapshot.KEY)
        defaults.synchronize()
        // Swift sets this at launch to call WidgetCenter.reloadAllTimelines().
        IosWidgetReloader.reload?.invoke()
    }
}

/** Swift installs a closure here so Kotlin can trigger a WidgetKit refresh. */
object IosWidgetReloader {
    var reload: (() -> Unit)? = null
}

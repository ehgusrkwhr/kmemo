package com.kmemo.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import androidx.glance.appwidget.updateAll
import com.kmemo.db.KmemoDatabase
import com.kmemo.widget.MemoWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

actual fun nowMillis(): Long = System.currentTimeMillis()

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun create(): SqlDriver =
        AndroidSqliteDriver(KmemoDatabase.Schema, context, "kmemo.db")
}

actual class WidgetSnapshotPublisher(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    actual fun publish(json: String) {
        context.getSharedPreferences(WidgetSnapshot.KEY, Context.MODE_PRIVATE)
            .edit()
            .putString(WidgetSnapshot.KEY, json)
            .apply()
        scope.launch { MemoWidget().updateAll(context) }
    }
}

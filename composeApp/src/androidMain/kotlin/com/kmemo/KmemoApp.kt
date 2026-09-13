package com.kmemo

import android.app.Application
import com.kmemo.data.DatabaseDriverFactory
import com.kmemo.data.WidgetSnapshotPublisher

/** Holds the singleton object graph for the app process (and the widget). */
class KmemoApp : Application() {
    lateinit var module: AppModule
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        module = AppModule(
            driverFactory = DatabaseDriverFactory(this),
            snapshotPublisher = WidgetSnapshotPublisher(this),
        )
    }

    companion object {
        lateinit var instance: KmemoApp
            private set
    }
}

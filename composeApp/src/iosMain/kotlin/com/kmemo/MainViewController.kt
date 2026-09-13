package com.kmemo

import androidx.compose.ui.window.ComposeUIViewController
import com.kmemo.data.DatabaseDriverFactory
import com.kmemo.data.WidgetSnapshotPublisher
import com.kmemo.ui.App
import com.kmemo.ui.MemoViewModel

/** Entry point consumed by the SwiftUI app to embed the shared Compose UI. */
fun MainViewController() = ComposeUIViewController {
    App(IosApp.viewModel)
}

/** Process-wide singletons shared by the iOS app. */
object IosApp {
    private val module = AppModule(
        driverFactory = DatabaseDriverFactory(),
        snapshotPublisher = WidgetSnapshotPublisher(),
    )
    val viewModel: MemoViewModel by lazy { module.createViewModel() }
}

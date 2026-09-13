package com.kmemo

import com.kmemo.data.DatabaseDriverFactory
import com.kmemo.data.MemoRepository
import com.kmemo.data.WidgetSnapshotPublisher
import com.kmemo.ui.MemoViewModel
import kotlinx.coroutines.Dispatchers

/** Builds the object graph the UI needs from platform-provided factories. */
class AppModule(
    driverFactory: DatabaseDriverFactory,
    snapshotPublisher: WidgetSnapshotPublisher,
) {
    private val repository = MemoRepository(
        driverFactory = driverFactory,
        ioDispatcher = Dispatchers.Default,
        snapshotPublisher = snapshotPublisher,
    )

    fun createViewModel(): MemoViewModel = MemoViewModel(repository)
}

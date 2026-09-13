package com.kmemo.data

import app.cash.sqldelight.db.SqlDriver

/** Platform-specific SQL driver creation. */
expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}

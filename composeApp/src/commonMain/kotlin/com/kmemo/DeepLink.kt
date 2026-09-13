package com.kmemo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Bridge for "open this memo" requests coming from a home-screen widget tap.
 * Platform code parses the `kmemo://memo/{id}` URL and calls [requestOpen];
 * the Compose UI observes [openMemoId] and opens the editor.
 */
object DeepLink {
    private val _openMemoId = MutableStateFlow<Long?>(null)
    val openMemoId: StateFlow<Long?> = _openMemoId

    fun requestOpen(id: Long) {
        _openMemoId.value = id
    }

    fun consumed() {
        _openMemoId.value = null
    }
}

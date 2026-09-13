package com.kmemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kmemo.data.Memo
import com.kmemo.data.MemoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MemoViewModel(private val repository: MemoRepository) : ViewModel() {

    val memos: StateFlow<List<Memo>> =
        repository.memos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun save(memo: Memo) {
        viewModelScope.launch { repository.upsert(memo) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }

    fun togglePin(memo: Memo) {
        viewModelScope.launch { repository.togglePin(memo) }
    }
}

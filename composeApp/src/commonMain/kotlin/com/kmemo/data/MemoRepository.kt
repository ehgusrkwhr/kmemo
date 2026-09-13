package com.kmemo.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kmemo.db.KmemoDatabase
import com.kmemo.db.MemoEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MemoRepository(
    driverFactory: DatabaseDriverFactory,
    private val ioDispatcher: CoroutineDispatcher,
    private val snapshotPublisher: WidgetSnapshotPublisher,
) {
    private val db = KmemoDatabase(driverFactory.create())
    private val queries = db.memoQueries

    val memos: Flow<List<Memo>> =
        queries.selectAll()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows -> rows.map(MemoEntity::toMemo) }

    suspend fun upsert(memo: Memo): Long = withContext(ioDispatcher) {
        val now = nowMillis()
        val id = if (memo.id == 0L) {
            queries.transactionWithResult {
                queries.insert(
                    title = memo.title,
                    content = memo.content,
                    color = memo.color,
                    pinned = if (memo.pinned) 1L else 0L,
                    updatedAt = now,
                )
                queries.lastInsertedId().executeAsOne()
            }
        } else {
            queries.update(
                title = memo.title,
                content = memo.content,
                color = memo.color,
                pinned = if (memo.pinned) 1L else 0L,
                updatedAt = now,
                id = memo.id,
            )
            memo.id
        }
        refreshWidget()
        id
    }

    suspend fun delete(id: Long) = withContext(ioDispatcher) {
        queries.deleteById(id)
        refreshWidget()
    }

    suspend fun togglePin(memo: Memo) {
        upsert(memo.copy(pinned = !memo.pinned))
    }

    private fun refreshWidget() {
        val current = queries.selectAll().executeAsList().map(MemoEntity::toMemo)
        snapshotPublisher.publish(WidgetSnapshot.encode(current))
    }
}

private fun MemoEntity.toMemo(): Memo = Memo(
    id = id,
    title = title,
    content = content,
    color = color,
    pinned = pinned == 1L,
    updatedAt = updatedAt,
)

/** Current epoch millis (platform-agnostic). */
expect fun nowMillis(): Long

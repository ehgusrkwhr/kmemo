package com.kmemo.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.kmemo.MainActivity
import com.kmemo.data.Memo
import com.kmemo.data.WidgetSnapshot
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

class MemoWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val raw = context
            .getSharedPreferences(WidgetSnapshot.KEY, Context.MODE_PRIVATE)
            .getString(WidgetSnapshot.KEY, "[]") ?: "[]"
        val memos = runCatching { json.decodeFromString<List<Memo>>(raw) }.getOrDefault(emptyList())
        provideContent { WidgetBody(memos) }
    }
}

class MemoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MemoWidget()
}

@androidx.compose.runtime.Composable
private fun WidgetBody(memos: List<Memo>) {
    val context = LocalContext.current
    Box(
        GlanceModifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(8.dp)
            .clickable(actionStartActivity(openIntent(context, null))),
    ) {
        if (memos.isEmpty()) {
            Text("메모가 없습니다", style = TextStyle(color = ColorProvider(Color(0xFF888888))))
        } else {
            LazyColumn(GlanceModifier.fillMaxSize()) {
                items(memos, itemId = { it.id }) { memo -> WidgetMemoCard(memo, context) }
            }
        }
    }
}

/** Intent that opens the app, optionally deep-linking to a specific memo. */
private fun openIntent(context: Context, memoId: Long?): Intent =
    Intent(context, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (memoId != null) {
            action = Intent.ACTION_VIEW
            data = Uri.parse("kmemo://memo/$memoId")
        }
    }

@androidx.compose.runtime.Composable
private fun WidgetMemoCard(memo: Memo, context: Context) {
    Column(
        GlanceModifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .background(Color(memo.color))
            .cornerRadius(12.dp)
            .clickable(actionStartActivity(openIntent(context, memo.id)))
            .padding(10.dp),
    ) {
        if (memo.title.isNotBlank()) {
            Text(
                memo.title,
                style = TextStyle(color = ColorProvider(Color.Black), fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
        }
        if (memo.content.isNotBlank()) {
            Text(
                memo.content,
                style = TextStyle(color = ColorProvider(Color(0xDD000000))),
                maxLines = 3,
            )
        }
    }
}

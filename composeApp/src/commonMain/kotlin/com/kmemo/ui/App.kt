package com.kmemo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kmemo.DeepLink
import com.kmemo.data.Memo
import com.kmemo.data.MemoColors
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(viewModel: MemoViewModel) {
    MaterialTheme {
        val memos by viewModel.memos.collectAsStateWithLifecycle()
        val openMemoId by DeepLink.openMemoId.collectAsStateWithLifecycle()
        var editing by remember { mutableStateOf<Memo?>(null) }

        // Open the editor when a widget deep link asks for a specific memo.
        LaunchedEffect(openMemoId, memos) {
            val id = openMemoId ?: return@LaunchedEffect
            val target = memos.firstOrNull { it.id == id }
            if (target != null) {
                editing = target
                DeepLink.consumed()
            }
        }

        if (editing != null) {
            MemoEditor(
                memo = editing!!,
                onSave = {
                    viewModel.save(it)
                    editing = null
                },
                onDelete = {
                    if (it.id != 0L) viewModel.delete(it.id)
                    editing = null
                },
                onClose = { editing = null },
            )
        } else {
            MemoListScreen(
                memos = memos,
                onOpen = { editing = it },
                onNew = { editing = Memo() },
                onTogglePin = viewModel::togglePin,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoListScreen(
    memos: List<Memo>,
    onOpen: (Memo) -> Unit,
    onNew: () -> Unit,
    onTogglePin: (Memo) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("메모") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNew) {
                Icon(Icons.Default.Add, contentDescription = "새 메모")
            }
        },
    ) { padding ->
        if (memos.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "메모가 없습니다.\n오른쪽 아래 + 버튼으로 추가하세요.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(160.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
            ) {
                items(memos, key = { it.id }) { memo ->
                    MemoCard(memo = memo, onClick = { onOpen(memo) }, onTogglePin = { onTogglePin(memo) })
                }
            }
        }
    }
}

@Composable
private fun MemoCard(memo: Memo, onClick: () -> Unit, onTogglePin: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(memo.color), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (memo.title.isNotBlank()) {
                Text(
                    memo.title,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Box(Modifier.weight(1f))
            }
            IconButton(onClick = onTogglePin, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.PushPin,
                    contentDescription = "고정",
                    tint = if (memo.pinned) Color(0xFFD84315) else Color(0x66000000),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        if (memo.content.isNotBlank()) {
            Text(
                memo.content,
                color = Color(0xDD000000),
                maxLines = 8,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoEditor(
    memo: Memo,
    onSave: (Memo) -> Unit,
    onDelete: (Memo) -> Unit,
    onClose: () -> Unit,
) {
    var title by remember { mutableStateOf(memo.title) }
    var content by remember { mutableStateOf(memo.content) }
    var color by remember { mutableStateOf(memo.color) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (memo.id == 0L) "새 메모" else "메모 편집") },
                navigationIcon = { TextButton(onClick = onClose) { Text("취소") } },
                actions = {
                    if (memo.id != 0L) {
                        IconButton(onClick = { onDelete(memo) }) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제")
                        }
                    }
                    IconButton(onClick = {
                        onSave(memo.copy(title = title.trim(), content = content.trim(), color = color))
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "저장")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("내용") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).weight(1f),
            )
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MemoColors.palette.forEach { swatch ->
                    Box(
                        Modifier
                            .size(34.dp)
                            .background(Color(swatch), CircleShape)
                            .clickable { color = swatch },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (swatch == color) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

package com.kmemo.data

import kotlinx.serialization.Serializable

/** A single note. [color] is a packed ARGB value used by the UI and widgets. */
@Serializable
data class Memo(
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val color: Long = MemoColors.default,
    val pinned: Boolean = false,
    val updatedAt: Long = 0L,
)

/** Preset note background colors (ARGB, opaque). */
object MemoColors {
    val default = 0xFFFFF6C3
    val palette: List<Long> = listOf(
        0xFFFFF6C3, // yellow
        0xFFFFD8CC, // salmon
        0xFFCDEFE0, // mint
        0xFFCFE6FF, // sky
        0xFFE7D9FF, // lavender
        0xFFF2F2F2, // gray
    )
}

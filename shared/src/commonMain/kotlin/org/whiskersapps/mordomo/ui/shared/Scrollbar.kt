package org.whiskersapps.mordomo.ui.shared

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.verticalScrollbar(
    state: ScrollState,
    width: Dp = 4.dp,
    color: Color = Color.Gray.copy(alpha = 0.5f)
): Modifier = drawWithContent {
    drawContent()
    if (state.maxValue == 0 || state.maxValue == Int.MAX_VALUE) return@drawWithContent
    val viewport = size.height
    val total = viewport + state.maxValue
    val thumbHeight = viewport * viewport / total
    val thumbY = state.value * viewport / total
    val w = width.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(size.width - w, thumbY),
        size = Size(w, thumbHeight),
        cornerRadius = CornerRadius(w / 2)
    )
}
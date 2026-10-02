package org.whiskersapps.mordomo.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@Composable
fun Modifier.bubblePadding(): Modifier {
    return this.padding(
        top = 12.dp,
        bottom = 12.dp,
        start = 16.dp,
        end = 16.dp,
    )
}

@Composable
fun Modifier.bubbleShape(index: Int, listSize: Int): Modifier {
    val singleShape = RoundedCornerShape(16.dp)
    val topShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
    val middleShape = RoundedCornerShape(4.dp)
    val bottomShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)

    if (listSize == 1)
        return this.clip(singleShape)

    if (index == 0)
        return this.clip(topShape)

    if (index == listSize - 1)
        return this.clip(bottomShape)

    return this.clip(middleShape)
}

@Composable
fun Modifier.buttonBubble(index: Int, listSize: Int, onClick: () -> Unit): Modifier {
    return this.bubbleShape(index, listSize)
        .background(LocalTheme.current.secondary)
        .clickable { onClick() }
        .bubblePadding()
}

@Composable
fun Modifier.bubble(index: Int, listSize: Int): Modifier {
    return this.bubbleShape(index, listSize)
        .background(LocalTheme.current.secondary)
        .bubblePadding()
}
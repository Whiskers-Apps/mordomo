package org.whiskersapps.mordomo.ui.main_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import org.whiskersapps.mordomo.core.features.entries.Entry
import org.whiskersapps.mordomo.core.utils.getImageFromPath
import org.whiskersapps.mordomo.ui.shared.MordomoTheme
import kotlin.time.Duration.Companion.milliseconds
import org.whiskersapps.mordomo.ui.main_screen.MainScreenIntent as Intent
import org.whiskersapps.mordomo.ui.main_screen.MainScreenState as State

@Composable
fun MainScreenRoot(
    vm: MainScreenVM = koinInject()
) {
    val state = vm.state.collectAsStateWithLifecycle().value

    MainScreen(state) { vm.onIntent(it) }
}

@Composable
fun MainScreen(
    state: State,
    onIntent: (Intent) -> Unit
) {
    MordomoTheme {
        val listState = rememberLazyListState()
        val focusRequester = remember { FocusRequester() }
        val searchFocusRequester = remember { FocusRequester() }
        val density = LocalDensity.current
        val windowInfo = LocalWindowInfo.current

        LaunchedEffect(windowInfo.isWindowFocused) {
            delay(100.milliseconds)

            if (windowInfo.isWindowFocused) {
                searchFocusRequester.requestFocus()
            }
        }

        LaunchedEffect(state.selectionIndex) {
            if (state.entries.isEmpty()) return@LaunchedEffect

            val layoutInfo = listState.layoutInfo
            val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == state.selectionIndex }

            val isFullyVisible = itemInfo != null &&
                    itemInfo.offset >= layoutInfo.viewportStartOffset &&
                    (itemInfo.offset + itemInfo.size) <= layoutInfo.viewportEndOffset

            if (!isFullyVisible) {
                listState.animateScrollToItem(state.selectionIndex)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { event ->
                    when (event.key) {
                        Key.Escape if event.type == KeyEventType.KeyDown -> {
                            onIntent(Intent.CloseWindow)
                            true
                        }

                        Key.DirectionDown if event.type == KeyEventType.KeyDown -> {
                            onIntent(Intent.ArrowDownClick)
                            true
                        }

                        Key.DirectionUp if event.type == KeyEventType.KeyDown -> {
                            onIntent(Intent.ArrowUpClick)
                            true
                        }

                        Key.Enter if event.type == KeyEventType.KeyDown -> {
                            onIntent(Intent.EnterClick)
                            true
                        }

                        else -> {
                            false
                        }
                    }
                }
        ) {
            BasicTextField(
                modifier = Modifier.focusRequester(searchFocusRequester),
                value = state.searchText,
                onValueChange = { onIntent(Intent.SearchTextType(it)) },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(items = state.entries) { index, entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .conditional(state.selectionIndex == index) {
                                background(MaterialTheme.colorScheme.surfaceVariant)
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        entry.image?.let {
                            val painter = remember(entry.image, density) {
                                getImageFromPath(entry.image, density)
                            }

                            painter?.let {
                                Image(
                                    modifier = Modifier.size(40.dp),
                                    painter = painter,
                                    contentDescription = null,
                                )

                                Spacer(Modifier.width(16.dp))
                            }
                        }


                        Column {
                            Text(text = entry.title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)

                            entry.description?.let {
                                Text(
                                    text = entry.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.conditional(
    condition: Boolean,
    modifier: @Composable Modifier.() -> Modifier
): Modifier = composed {
    if (condition) {
        then(modifier())
    } else {
        this
    }
}


@Composable
@Preview(showBackground = true)
fun MainScreenPreview() {
    val state = State(
        searchText = "",
        entries = listOf(
            Entry(
                image = "/usr/share/icons/Papirus/128x128/apps/localsend_app.svg",
                title = "Localsend",
                description = "Application"
            ),
            Entry(
                image = "/usr/share/icons/Papirus/128x128/apps/firefox.svg",
                title = "Firefox",
                description = "Fast and private browser"
            ),
            Entry(
                image = "/usr/share/icons/Papirus/64x64/apps/system-file-manager.svg",
                title = "Files",
                description = null
            ),
        )
    )

    MainScreen(state) {}
}
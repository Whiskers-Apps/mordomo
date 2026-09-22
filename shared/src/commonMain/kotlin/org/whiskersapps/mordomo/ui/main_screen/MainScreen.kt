package org.whiskersapps.mordomo.ui.main_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
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
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lib.Entry
import mordomo.shared.generated.resources.Res
import mordomo.shared.generated.resources.base_icon
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.whiskersapps.mordomo.core.utils.getImageFromPath
import org.whiskersapps.mordomo.ui.shared.MordomoTheme
import org.whiskersapps.mordomo.ui.main_screen.MainScreenIntent as Intent
import org.whiskersapps.mordomo.ui.main_screen.MainScreenState as State

@Composable
fun MainScreenRoot(
    vm: MainScreenVM = koinInject()
) {
    val state = vm.state.collectAsState().value

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
        val scope = rememberCoroutineScope()

        // This is not the most performant but NOTHING works so yolo
        if (state.focus && windowInfo.isWindowFocused) {
            scope.launch { searchFocusRequester.requestFocus() }
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

                        Key.S if event.type == KeyEventType.KeyDown && event.isCtrlPressed-> {
                            onIntent(Intent.SettingsShortcutClick)
                            true
                        }

                        else -> {
                            false
                        }
                    }
                }
        ) {
            Column(
                Modifier.fillMaxHeight()
                    .weight(1f)
            ) {
                Box(Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp),contentAlignment = Alignment.CenterStart) {
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
                    if (state.searchText.isEmpty()) {
                        Text("Search")
                    }
                }


                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(items = state.entries) { index, entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .conditional(state.selectionIndex == index) {
                                    background(MaterialTheme.colorScheme.surfaceVariant)
                                }
                                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            entry.image?.let { image ->
                                val painter = remember(image, density) {
                                    getImageFromPath(image, density)
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
                                Text(
                                    text = entry.title,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                entry.description?.let { description ->
                                    Text(
                                        text = description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Image(
                        modifier = Modifier.height(24.dp),
                        painter = painterResource(Res.drawable.base_icon),
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = state.context,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp
                    )
                }


                Text(
                    text = if (state.entries.isEmpty()) "No Results" else "${state.entries.size} Results",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )
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
        ),
        context = "Apps"
    )

    MainScreen(state) {}
}
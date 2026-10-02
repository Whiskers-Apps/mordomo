package org.whiskersapps.mordomo.ui.settings_screen.search_engines_tab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mordomo.shared.generated.resources.Res
import mordomo.shared.generated.resources.check
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.whiskersapps.mordomo.core.utils.getEngineFaviconPath
import org.whiskersapps.mordomo.core.utils.getImageFromPath
import org.whiskersapps.mordomo.ui.settings_screen.search_engines_tab.composables.AddSearchEngineDialog
import org.whiskersapps.mordomo.ui.settings_screen.search_engines_tab.composables.SearchEngineDialog
import org.whiskersapps.mordomo.ui.settings_screen.shared_composables.TextSection
import org.whiskersapps.mordomo.ui.shared.LocalTheme
import org.whiskersapps.mordomo.ui.shared.bubble
import org.whiskersapps.mordomo.ui.shared.buttonBubble
import org.whiskersapps.mordomo.ui.settings_screen.search_engines_tab.SearchEnginesTabIntent as Intent
import org.whiskersapps.mordomo.ui.settings_screen.search_engines_tab.SearchEnginesTabState as State

@Composable
fun SearchEnginesTabRoot(
    vm: SearchEnginesTabVM = koinInject()
) {
    SearchEnginesTab(
        state = vm.state.collectAsState().value,
        onIntent = { vm.onIntent(it) }
    )
}

@Composable
fun SearchEnginesTab(
    state: State,
    onIntent: (Intent) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current

    if (state.showAddDialog) {
        AddSearchEngineDialog(
            onCancel = { onIntent(Intent.AddDialogClose) },
            onSave = { name, keyword, query, default -> onIntent(Intent.SaveNewEngineClick(name, keyword, query, default)) },
        )
    }

    LazyColumn {
        item {
            TextSection(
                focusRequester = focusRequester,
                title = "Search Keyword",
                description = "The keyword to search using the default engine",
                value = state.searchKeyword ?: "",
                onValueChange = { onIntent(Intent.KeywordType(it)) }
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onIntent(Intent.AddClick) }
            ) {
                Text(
                    text = "Add",
                )
            }

            Spacer(Modifier.height(16.dp))
        }

        itemsIndexed(
            items = state.searchEngines,
            key = { _, searchEngine -> searchEngine.id }
        ) { index, searchEngine ->
            val painter = remember(searchEngine.query) {
                getImageFromPath(getEngineFaviconPath(searchEngine.id), density)
            }

            var showDialog by remember { mutableStateOf(false) }

            if (showDialog) {
                SearchEngineDialog(
                    searchEngine = searchEngine,
                    defaultEngineId = state.defaultEngineId,
                    onCancel = { showDialog = false },
                    onDelete = {
                        showDialog = false
                        onIntent(Intent.DeleteEngineClick(searchEngine.id))
                    },
                    onSave = { searchEngine, default ->
                        showDialog = false
                        onIntent(Intent.SaveSearchEngineClick(searchEngine, default))
                    }
                )
            }

            Row(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .buttonBubble(index, state.searchEngines.size) {
                        showDialog = true
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                painter?.let {
                    Image(
                        modifier = Modifier.size(40.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        painter = painter,
                        contentDescription = null,
                    )

                    Spacer(Modifier.width(16.dp))
                }

                searchEngine.keyword?.let { keyword ->
                    Box(
                        Modifier.clip(RoundedCornerShape(8.dp))
                            .background(LocalTheme.current.main)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = keyword,
                            color = LocalTheme.current.textMain
                        )
                    }

                    Spacer(Modifier.width(16.dp))
                }

                Column(Modifier.fillMaxWidth().weight(1f)) {
                    Text(
                        text = searchEngine.name,
                        color = LocalTheme.current.textMain,
                    )

                    Text(
                        text = searchEngine.query,
                        color = LocalTheme.current.textSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.width(16.dp))

                if (searchEngine.id == state.defaultEngineId) {
                    Box(
                        Modifier.clip(CircleShape)
                            .background(LocalTheme.current.main)
                            .padding(8.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(Res.drawable.check),
                            contentDescription = null,
                            tint = LocalTheme.current.textMain
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun SearchEnginesTabPreview() {
    SearchEnginesTab(State()) {}
}
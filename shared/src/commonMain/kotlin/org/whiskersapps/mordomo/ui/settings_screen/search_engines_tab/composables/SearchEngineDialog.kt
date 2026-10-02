package org.whiskersapps.mordomo.ui.settings_screen.search_engines_tab.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import lib.SearchEngine
import org.whiskersapps.mordomo.ui.settings_screen.shared_composables.SwitchSection
import org.whiskersapps.mordomo.ui.shared.LocalTheme
import org.whiskersapps.mordomo.ui.shared.verticalScrollbar

@Composable
fun SearchEngineDialog(
    searchEngine: SearchEngine,
    defaultEngineId: Int?,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onSave: (searchEngine: SearchEngine, default: Boolean) -> Unit,
) {
    var name by remember(searchEngine.name) { mutableStateOf(searchEngine.name) }
    var keyword by remember(searchEngine.keyword) { mutableStateOf(searchEngine.keyword ?: "") }
    var query by remember(searchEngine.query) { mutableStateOf(searchEngine.query) }
    var default by remember(defaultEngineId) { mutableStateOf(defaultEngineId == searchEngine.id) }
    var clickedDelete by remember { mutableStateOf(false) }

    val enabled by remember(name, keyword, query) {
        mutableStateOf(
            name.isNotBlank() && query.isNotBlank() && query.contains("%s")
        )
    }

    val hasQueryKeyword by remember(query) { mutableStateOf(query.contains("%s")) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = { onCancel() },
    ) {
        Column(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(LocalTheme.current.main)
                .padding(24.dp)
        ) {
            Column(
                Modifier.fillMaxHeight()
                    .weight(1f)
                    .verticalScrollbar(scrollState)
                    .verticalScroll(scrollState)
            ) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column {
                        Text(
                            text = "Edit Search Engine",
                            color = LocalTheme.current.textMain
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Name",
                    color = LocalTheme.current.textMain
                )

                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Query",
                    color = LocalTheme.current.textMain
                )

                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp)
                )

                if (!hasQueryKeyword) {
                    Text(
                        text = """Query requires a "%s" in the url to know where to add the search text""",
                        color = LocalTheme.current.danger
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Keyword",
                    color = LocalTheme.current.textMain
                )

                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = keyword,
                    onValueChange = { keyword = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(16.dp))

                SwitchSection(
                    title = "Default",
                    description = "Make this the default search engine",
                    checked = default,
                    onCheckedChange = { default = it },
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = { onCancel() },
                ) {
                    Text(
                        text = "Cancel",
                        color = LocalTheme.current.textMain
                    )
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (clickedDelete) {
                            onDelete()
                        } else {
                            clickedDelete = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalTheme.current.danger
                    )
                ) {
                    Text(
                        text = if (clickedDelete) "Click Again to Delete" else "Delete",
                        color = LocalTheme.current.onDanger
                    )
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                        onSave(
                            searchEngine.copy(
                                name = name,
                                keyword = keyword.ifBlank { null },
                                query = query,
                            ),
                            default
                        )
                    },
                    enabled = enabled,
                ) {
                    Text(
                        text = "Save",
                        color = LocalTheme.current.onAccent
                    )
                }
            }
        }
    }
}
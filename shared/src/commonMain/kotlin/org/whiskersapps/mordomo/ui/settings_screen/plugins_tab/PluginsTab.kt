package org.whiskersapps.mordomo.ui.settings_screen.plugins_tab


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lib.CheckSetting
import lib.NumberSetting
import lib.SelectSetting
import lib.TextSetting
import mordomo.shared.generated.resources.Res
import mordomo.shared.generated.resources.chevron_down
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.whiskersapps.mordomo.ui.settings_screen.plugins_tab.PluginsTabIntent as Intent
import org.whiskersapps.mordomo.ui.settings_screen.plugins_tab.PluginsTabState as State
import org.whiskersapps.mordomo.ui.settings_screen.plugins_tab.PluginsTabVM as VM

@Composable
fun PluginsTabRoot(
    vm: VM = koinInject()
) {
    PluginsTab(
        state = vm.state.collectAsState().value,
        onIntent = { vm.onIntent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginsTab(
    state: State,
    onIntent: (Intent) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = state.manifests,
            key = { it.id }
        ) { manifest ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(top = 16.dp, end = 16.dp, start = 16.dp)
            ) {
                Text(
                    text = manifest.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )

                Text(
                    text = manifest.description,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Keyword",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(2.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.values[manifest.id]!!["[keyword]"]!!,
                    onValueChange = {
                        onIntent(
                            Intent.SettingChange(
                                pluginId = manifest.id,
                                settingId = "[keyword]",
                                value = it
                            )
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                )

                if (manifest.settings.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))

                    HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
                }

                Spacer(Modifier.height(16.dp))

                for (setting in manifest.settings) {
                    when (setting) {
                        is CheckSetting -> {
                            val settingValue = state.values[manifest.id]!![setting.id]!!

                            Row(Modifier.fillMaxWidth()) {
                                Column(Modifier.fillMaxWidth().weight(1f)) {
                                    Text(
                                        text = setting.title,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Medium,
                                    )

                                    Text(
                                        text = setting.description,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }

                                Spacer(Modifier.width(16.dp))

                                Switch(
                                    checked = settingValue == "true",
                                    onCheckedChange = { checked ->
                                        onIntent(
                                            Intent.SettingChange(
                                                pluginId = manifest.id,
                                                settingId = setting.id,
                                                value = checked.toString()
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        is NumberSetting -> {
                            val settingValue = state.values[manifest.id]!![setting.id]!!

                            Text(
                                text = setting.title,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Medium,
                            )

                            Text(
                                text = setting.description,
                                color = MaterialTheme.colorScheme.onBackground,
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                value = settingValue,
                                onValueChange = {
                                    if (it.toIntOrNull() != null || it.isEmpty()) {
                                        onIntent(
                                            Intent.SettingChange(
                                                pluginId = manifest.id,
                                                settingId = setting.id,
                                                value = it
                                            )
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                    focusedContainerColor = MaterialTheme.colorScheme.background,
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                singleLine = true,
                            )
                        }

                        is SelectSetting -> {
                            val settingValue = state.values[manifest.id]!![setting.id]!!
                            var expanded by remember { mutableStateOf(false) }

                            Text(
                                text = setting.title,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Medium,
                            )

                            Text(
                                text = setting.description,
                                color = MaterialTheme.colorScheme.onBackground,
                            )

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                            ) {
                                Column(
                                    Modifier.clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.background)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                1.dp,
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            .clickable {
                                                expanded = !expanded
                                            }
                                            .padding(16.dp),
                                        verticalAlignment = CenterVertically
                                    ) {
                                        Text(
                                            modifier = Modifier.fillMaxWidth().weight(1f),
                                            text = setting.options.first { it.id == settingValue }.text,
                                        )

                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            painter = painterResource(Res.drawable.chevron_down),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onBackground,
                                        )
                                    }

                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                    ) {
                                        setting.options.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(text = option.text) },
                                                onClick = {
                                                    onIntent(
                                                        Intent.SettingChange(
                                                            pluginId = manifest.id,
                                                            settingId = setting.id,
                                                            value = option.id
                                                        )
                                                    )

                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        is TextSetting -> {
                            val settingValue = state.values[manifest.id]!![setting.id]!!

                            Text(
                                text = setting.title,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Medium,
                            )

                            Text(
                                text = setting.description,
                                color = MaterialTheme.colorScheme.onBackground,
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                value = settingValue,
                                onValueChange = {
                                    onIntent(
                                        Intent.SettingChange(
                                            pluginId = manifest.id,
                                            settingId = setting.id,
                                            value = it
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                    focusedContainerColor = MaterialTheme.colorScheme.background,
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                singleLine = true,
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
@Preview
fun PluginsTabPreview() {
    PluginsTab(State()) {}
}
package org.whiskersapps.mordomo.ui.settings_screen.plugins_tab


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lib.CheckSetting
import lib.NumberSetting
import lib.SelectSetting
import lib.TextSetting
import org.koin.compose.koinInject
import org.whiskersapps.mordomo.ui.settings_screen.shared_composables.NumberSection
import org.whiskersapps.mordomo.ui.settings_screen.shared_composables.SelectSection
import org.whiskersapps.mordomo.ui.settings_screen.shared_composables.SwitchSection
import org.whiskersapps.mordomo.ui.settings_screen.shared_composables.TextSection
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
                            SwitchSection(
                                title = setting.title,
                                description = setting.description,
                                checked = state.values[manifest.id]!![setting.id]!! == "true",
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

                        is NumberSetting -> {
                            NumberSection(
                                focusRequester = focusRequester,
                                title = setting.title,
                                description = setting.description,
                                value = state.values[manifest.id]!![setting.id]!!,
                                onValueChange = {
                                    onIntent(
                                        Intent.SettingChange(
                                            pluginId = manifest.id,
                                            settingId = setting.id,
                                            value = it
                                        )
                                    )
                                }
                            )
                        }

                        is SelectSetting -> {
                            SelectSection(
                                title = setting.title,
                                description = setting.description,
                                options = setting.options,
                                value = state.values[manifest.id]!![setting.id]!!,
                                onSelect = { option ->
                                    onIntent(
                                        Intent.SettingChange(
                                            pluginId = manifest.id,
                                            settingId = setting.id,
                                            value = option.id
                                        )
                                    )
                                }
                            )
                        }

                        is TextSetting -> {
                            TextSection(
                                focusRequester = focusRequester,
                                title = setting.title,
                                description = setting.description,
                                value = state.values[manifest.id]!![setting.id]!!,
                                onValueChange = {
                                    onIntent(
                                        Intent.SettingChange(
                                            pluginId = manifest.id,
                                            settingId = setting.id,
                                            value = it
                                        )
                                    )
                                }
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
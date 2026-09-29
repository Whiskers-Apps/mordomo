package org.whiskersapps.mordomo.ui.settings_screen

import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mordomo.shared.generated.resources.Res
import mordomo.shared.generated.resources.arrow_left
import mordomo.shared.generated.resources.beverage
import mordomo.shared.generated.resources.file
import mordomo.shared.generated.resources.home
import mordomo.shared.generated.resources.info
import mordomo.shared.generated.resources.palette
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.whiskersapps.mordomo.ui.settings_screen.about_tab.AboutTab
import org.whiskersapps.mordomo.ui.settings_screen.about_tab.AboutTabRoot
import org.whiskersapps.mordomo.ui.settings_screen.dev_tab.DevTab
import org.whiskersapps.mordomo.ui.settings_screen.general_tab.GeneralTab
import org.whiskersapps.mordomo.ui.settings_screen.plugins_tab.PluginsTab
import org.whiskersapps.mordomo.ui.settings_screen.plugins_tab.PluginsTabRoot
import org.whiskersapps.mordomo.ui.settings_screen.theme_tab.ThemeTab
import org.whiskersapps.mordomo.ui.settings_screen.SettingsScreenIntent as Intent
import org.whiskersapps.mordomo.ui.settings_screen.SettingsScreenVM as VM

@Composable
fun SettingsScreenRoot(
    vm: VM = koinInject()
) {
    SettingsScreen(
        state = vm.state.collectAsState().value,
        onIntent = { vm.onIntent(it) },
    )
}

@Composable
fun SettingsScreen(
    state: SettingsScreenState,
    onIntent: (Intent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            Modifier.fillMaxHeight()
                .width(IntrinsicSize.Min)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Tab(
                icon = Res.drawable.home,
                text = "General",
                selected = state.tab == SettingsTab.General,
                onClick = { onIntent(Intent.TabSelect(SettingsTab.General)) }
            )

            Tab(
                icon = Res.drawable.palette,
                text = "Theme",
                selected = state.tab == SettingsTab.Theme,
                onClick = { onIntent(Intent.TabSelect(SettingsTab.Theme)) }
            )

            Tab(
                icon = Res.drawable.file,
                text = "Plugins",
                selected = state.tab == SettingsTab.Plugins,
                onClick = { onIntent(Intent.TabSelect(SettingsTab.Plugins)) }
            )

            Tab(
                icon = Res.drawable.info,
                text = "About",
                selected = state.tab == SettingsTab.About,
                onClick = { onIntent(Intent.TabSelect(SettingsTab.About)) }
            )

            Tab(
                icon = Res.drawable.beverage,
                text = "Dev",
                selected = state.tab == SettingsTab.Dev,
                onClick = { onIntent(Intent.TabSelect(SettingsTab.Dev)) }
            )
        }

        Column(Modifier.padding(24.dp)){
            Box(
                Modifier.clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        onIntent(Intent.Back)
                    }
                    .padding(8.dp)
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(Res.drawable.arrow_left),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(Modifier.height(16.dp))

            when (state.tab) {
                SettingsTab.General -> {
                    GeneralTab()
                }

                SettingsTab.Theme -> {
                    ThemeTab()
                }

                SettingsTab.Plugins -> {
                    PluginsTabRoot()
                }

                SettingsTab.About -> {
                    AboutTabRoot()
                }

                SettingsTab.Dev -> {
                    DevTab()
                }
            }
        }
    }
}

@Composable
fun Tab(icon: DrawableResource, text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clip(CircleShape)
            .fillMaxWidth()
            .background(if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.width(8.dp))

        Text(text = text, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
@Preview
fun SettingsScreenPreview() {
    SettingsScreen(SettingsScreenState()) {}
}
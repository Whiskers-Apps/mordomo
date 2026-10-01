package org.whiskersapps.mordomo.ui.settings_screen.theme_tab


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.SaturationSlider
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import org.koin.compose.koinInject
import org.whiskersapps.mordomo.ui.settings_screen.shared_composables.ColorSection
import org.whiskersapps.mordomo.ui.settings_screen.shared_composables.SwitchSection
import org.whiskersapps.mordomo.ui.shared.LocalTheme
import org.whiskersapps.mordomo.ui.settings_screen.theme_tab.ThemeTabIntent as Intent
import org.whiskersapps.mordomo.ui.settings_screen.theme_tab.ThemeTabState as State

@Composable
fun ThemeTabRoot(
    vm: ThemeTabVM = koinInject()
) {
    ThemeTab(
        state = vm.state.collectAsState().value,
        onIntent = { vm.onIntent(it) }
    )
}

@Composable
fun ThemeTab(
    state: State,
    onIntent: (Intent) -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        SwitchSection(
            title = "Dark",
            description = "This tells plugins if it's a dark theme",
            checked = state.theme.dark,
            onCheckedChange = { onIntent(Intent.DarkModeCheck(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ColorSection(
            title = "Main",
            description = "The main background color",
            hex = state.theme.main,
            onColorChange = { onIntent(Intent.MainPick(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ColorSection(
            title = "Secondary",
            description = "The secondary background color",
            hex = state.theme.secondary,
            onColorChange = { onIntent(Intent.SecondaryPick(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ColorSection(
            title = "Tertiary",
            description = "The tertiary background color",
            hex = state.theme.tertiary,
            onColorChange = { onIntent(Intent.TertiaryPick(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ColorSection(
            title = "Text Main",
            description = "The primary text color",
            hex = state.theme.textMain,
            onColorChange = { onIntent(Intent.TextMainPick(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ColorSection(
            title = "Text Secondary",
            description = "The secondary text color",
            hex = state.theme.textSecondary,
            onColorChange = { onIntent(Intent.TextSecondaryPick(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ColorSection(
            title = "Text Disabled",
            description = "The disabled text color",
            hex = state.theme.textDisabled,
            onColorChange = { onIntent(Intent.TextDisabledPick(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ColorSection(
            title = "Accent",
            description = "The accent color",
            hex = state.theme.accent,
            onColorChange = { onIntent(Intent.AccentPick(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ColorSection(
            title = "On Accent",
            description = "The color for elements on top of the accent color",
            hex = state.theme.onAccent,
            onColorChange = { onIntent(Intent.OnAccentPick(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ColorSection(
            title = "Danger",
            description = "The danger/error color",
            hex = state.theme.danger,
            onColorChange = { onIntent(Intent.DangerPick(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ColorSection(
            title = "On Danger",
            description = "The color for elements on top of the danger color",
            hex = state.theme.onDanger,
            onColorChange = { onIntent(Intent.OnDangerPick(it)) }
        )
    }
}

@Composable
@Preview
fun ThemeTabPreview() {
    ThemeTab(State()) {}
}
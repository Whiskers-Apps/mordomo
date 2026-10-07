package org.whiskersapps.mordomo.ui.settings_screen.general_tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.whiskersapps.mordomo.ui.settings_screen.shared_composables.SwitchSection
import org.whiskersapps.mordomo.ui.settings_screen.general_tab.GeneralTabIntent as Intent
import org.whiskersapps.mordomo.ui.settings_screen.general_tab.GeneralTabState as State

@Composable
fun GeneralTabRoot(
    vm: GeneralTabVM = koinInject()
) {
    GeneralTab(
        state = vm.state.collectAsState().value,
        onIntent = { vm.onIntent(it) }
    )
}

@Composable
fun GeneralTab(
    state: State,
    onIntent: (Intent) -> Unit,
) {
    if (!state.loading) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SwitchSection(
                title = "Hide Footer",
                description = "Hides the footer when searching",
                checked = state.hideFooter,
                onCheckedChange = { onIntent(Intent.HideFooterCheck(it)) },
            )

            SwitchSection(
                title = "Draw Border",
                description = "Draws a border around the app. Useful when there's no window decorations",
                checked = state.drawBorder,
                onCheckedChange = { onIntent(Intent.DrawBorderCheck(it)) },
            )
        }
    }
}

@Composable
@Preview
fun GeneralTabPreview() {
    GeneralTab(
        State(
            loading = false,
        )
    ) {}
}
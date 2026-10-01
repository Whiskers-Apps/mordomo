package org.whiskersapps.mordomo.ui.settings_screen.dev_tab


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import org.whiskersapps.mordomo.ui.shared.LocalTheme
import org.whiskersapps.mordomo.ui.settings_screen.dev_tab.DevTabIntent as Intent
import org.whiskersapps.mordomo.ui.settings_screen.dev_tab.DevTabState as State
import org.whiskersapps.mordomo.ui.settings_screen.dev_tab.DevTabVM as VM

@Composable
fun DevTabRoot(
    vm: VM = koinInject()
) {
    DevTab(
        state = vm.state.collectAsState().value,
        onIntent = { vm.onIntent(it) }
    )
}

@Composable
fun DevTab(
    state: State = koinInject(),
    onIntent: (Intent) -> Unit,
) {
    Row(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().weight(1f)) {
            Text(
                text = if (state.runningPlugins) "Stop Plugins" else "Start Plugins",
                color = LocalTheme.current.textMain,
            )

            Text(
                text = "Start/Stop running plugins",
                color = LocalTheme.current.textMain,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.width(16.dp))

        Button(
            onClick = { onIntent(Intent.StartStopClick) }
        ) {
            Text(
                text = if (state.runningPlugins) "Stop" else "Start",
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    Row(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().weight(1f)) {
            Text(
                text = "Re-Index Plugins",
                color = LocalTheme.current.textMain,
            )

            Text(
                text = "Detect new plugins and changes",
                color = LocalTheme.current.textMain,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.width(16.dp))

        Button(
            onClick = { onIntent(Intent.ReIndexClick) }
        ) {
            Text(
                text = "Re-Index",
            )
        }
    }
}

@Composable
@Preview
fun DevTabPreview() {
    DevTab {}
}
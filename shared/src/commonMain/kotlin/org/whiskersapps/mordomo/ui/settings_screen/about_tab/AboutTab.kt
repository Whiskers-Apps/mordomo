package org.whiskersapps.mordomo.ui.settings_screen.about_tab


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.whiskersapps.mordomo.ui.shared.LocalTheme
import org.whiskersapps.mordomo.ui.settings_screen.about_tab.AboutTabIntent as Intent
import org.whiskersapps.mordomo.ui.settings_screen.about_tab.AboutTabState as State

@Composable
fun AboutTabRoot(
    vm: AboutTabVM = koinInject()
) {
    AboutTab(
        state = vm.state.collectAsState().value,
        onIntent = { vm.onIntent(it) },
    )
}

@Composable
fun AboutTab(
    state: State,
    onIntent: (Intent) -> Unit,
) {
    Column {
        Text(
            text = "Version",
            color = LocalTheme.current.textMain,
            fontWeight = FontWeight.Medium,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = state.version,
            color = LocalTheme.current.textMain,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "License",
            color = LocalTheme.current.textMain,
            fontWeight = FontWeight.Medium,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "MIT",
            color = LocalTheme.current.textMain,
        )

        Spacer(Modifier.height(16.dp))

        Column(
            Modifier.fillMaxWidth()
                .clickable { onIntent(Intent.RepoClick) }
        ) {
            Text(
                text = "Repository",
                color = LocalTheme.current.textMain,
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "github.com/whiskers-apps/mordomo",
                color = LocalTheme.current.textMain,
            )
        }
    }
}

@Composable
@Preview
fun AboutTabPreview() {
    AboutTab(State()) {}
}
package org.whiskersapps.mordomo.ui.settings_screen.shared_composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.whiskersapps.mordomo.ui.shared.LocalTheme

@Composable
fun SwitchSection(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }) {
        Column(Modifier.fillMaxWidth().weight(1f)) {
            Text(
                text = title,
                color = LocalTheme.current.textMain,
                fontWeight = FontWeight.Medium,
            )

            Text(
                text = description,
                color = LocalTheme.current.textMain,
            )
        }

        Spacer(Modifier.width(16.dp))

        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) }
        )
    }
}
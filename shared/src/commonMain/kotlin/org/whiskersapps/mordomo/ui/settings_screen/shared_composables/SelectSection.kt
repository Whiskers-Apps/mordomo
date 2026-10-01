package org.whiskersapps.mordomo.ui.settings_screen.shared_composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lib.SelectOption
import lib.SelectSetting
import mordomo.shared.generated.resources.Res
import mordomo.shared.generated.resources.chevron_down
import org.jetbrains.compose.resources.painterResource
import org.whiskersapps.mordomo.ui.shared.LocalTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSection(
    title: String,
    description: String,
    options: List<SelectSetting.Option>,
    value: String,
    onSelect: (option: SelectSetting.Option) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Text(
        text = title,
        color = LocalTheme.current.textMain,
        fontWeight = FontWeight.Medium,
    )

    Text(
        text = description,
        color = LocalTheme.current.textMain,
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        Column(
            Modifier.clip(RoundedCornerShape(12.dp))
                .background(LocalTheme.current.main)
        ) {
            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        shape = RoundedCornerShape(12.dp),
                        color = LocalTheme.current.textMain
                    )
                    .clickable {
                        expanded = !expanded
                    }
                    .padding(16.dp),
                verticalAlignment = CenterVertically
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    text = options.first { it.id == value }.text,
                )

                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(Res.drawable.chevron_down),
                    contentDescription = null,
                    tint = LocalTheme.current.textMain,
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option.text) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
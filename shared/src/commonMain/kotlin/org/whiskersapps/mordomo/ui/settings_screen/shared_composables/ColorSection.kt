package org.whiskersapps.mordomo.ui.settings_screen.shared_composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.SaturationSlider
import com.github.skydoves.colorpicker.compose.SliderOrientation
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import org.whiskersapps.mordomo.ui.shared.LocalTheme
import org.whiskersapps.mordomo.ui.shared.toColor

@Composable
fun ColorSection(
    title: String,
    description: String,
    hex: String,
    onColorChange: (hex: String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val colorController = rememberColorPickerController().apply {
        debounceDuration = 200L
    }

    var fieldText by remember { mutableStateOf(hex.takeLast(6).removePrefix("#")) }
    val enabled by remember(fieldText) { mutableStateOf(Regex("^#[0-9A-Fa-f]{6}$").matches("#${fieldText}")) }

    if (expanded) {
        Dialog(
            onDismissRequest = { expanded = false },
        ) {
            Column(
                Modifier
                    .width(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(24.dp))
                    .background(LocalTheme.current.main)
                    .padding(24.dp)
            ) {

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    OutlinedTextField(
                        modifier = Modifier.width(140.dp),
                        value = fieldText,
                        onValueChange = { fieldText = it },
                        singleLine = true,
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    HsvColorPicker(
                        modifier = Modifier.width(200.dp).height(200.dp),
                        controller = colorController,
                        onColorChanged = { envelope ->
                            if(envelope.fromUser){
                                fieldText = envelope.hexCode.takeLast(6)
                            }
                        },
                        initialColor = hex.toColor(),
                    )

                    Spacer(Modifier.width(16.dp))

                    SaturationSlider(
                        modifier = Modifier.width(40.dp).height(200.dp),
                        controller = colorController,
                        orientation = SliderOrientation.Vertical
                    )

                    Spacer(Modifier.width(16.dp))

                    BrightnessSlider(
                        modifier = Modifier.width(40.dp).height(200.dp),
                        controller = colorController,
                        orientation = SliderOrientation.Vertical
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(
                        onClick = {
                            expanded = false
                            colorController.wheelColor = hex.toColor()
                            fieldText = hex.removePrefix("#")
                        }
                    ) {
                        Text(
                            text = "Cancel"
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onColorChange("#${fieldText}")
                            colorController.wheelColor = "#${fieldText}".toColor()
                            expanded = false
                        },
                        enabled = enabled
                    ) {
                        Text(text = "Save")
                    }
                }
            }
        }
    }

    Row(
        Modifier.fillMaxWidth()
            .clickable { expanded = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier.fillMaxWidth()
                .weight(1f)
        ) {
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

        Box(
            Modifier.border(width = 2.dp, color = LocalTheme.current.textMain, shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(hex.toColor())
                .height(40.dp)
                .width(80.dp)
        )
    }
}
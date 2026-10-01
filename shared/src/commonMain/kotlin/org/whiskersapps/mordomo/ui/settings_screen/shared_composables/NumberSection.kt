package org.whiskersapps.mordomo.ui.settings_screen.shared_composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.whiskersapps.mordomo.ui.shared.LocalTheme

@Composable
fun NumberSection(
    focusRequester: FocusRequester,
    title: String,
    description: String,
    value: String,
    onValueChange: (String) -> Unit,
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

    Spacer(modifier = Modifier.height(2.dp))

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        value = value,
        onValueChange = {
            if (it.toIntOrNull() != null || it.isEmpty())
                onValueChange(it)
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = LocalTheme.current.main,
            focusedContainerColor = LocalTheme.current.main,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        singleLine = true,
    )
}
package org.whiskersapps.mordomo.ui.form_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import lib.CheckInput
import lib.Form
import lib.NumberInput
import lib.PathInput
import lib.SelectInput
import lib.SelectOption
import lib.TextInput
import mordomo.shared.generated.resources.Res
import mordomo.shared.generated.resources.arrow_left
import mordomo.shared.generated.resources.base_icon
import mordomo.shared.generated.resources.chevron_down
import mordomo.shared.generated.resources.file
import mordomo.shared.generated.resources.folder
import mordomo.shared.generated.resources.trash
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.whiskersapps.mordomo.ui.form_screen.FormScreenIntent as Intent

@Composable
fun FormScreenRoot(
    vm: FormScreenVM = koinInject(),
) {
    val state = vm.state.collectAsState().value

    FormScreen(state) { vm.onIntent(it) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    state: State,
    onIntent: (Intent) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    Column(
        Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onPreviewKeyEvent { event ->
                when (event.key) {
                    Key.Escape -> {
                        onIntent(Intent.Back)
                        focusRequester.freeFocus()
                        true
                    }

                    else -> false
                }
            }
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        onIntent(Intent.Back)
                        focusRequester.freeFocus()
                    }
                    .padding(8.dp)
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(Res.drawable.arrow_left),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = state.form.title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            Modifier.fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items = state.form.inputs) { input ->
                Column {
                    when (input) {
                        is TextInput -> {
                            InputHeader(input.title, input.description)

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                value = input.value,
                                onValueChange = { onIntent(Intent.TextInput(input.id, it)) },
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                singleLine = true,
                            )
                        }

                        is NumberInput -> {
                            InputHeader(input.title, input.description)

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                value = if (input.value == 0) "" else input.value.toString(),
                                onValueChange = {
                                    if (it.toIntOrNull() != null || it.isEmpty()) {
                                        onIntent(Intent.NumberInput(input.id, it))
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                singleLine = true,
                            )
                        }

                        is CheckInput -> {
                            Row(Modifier.fillMaxWidth()) {
                                Column(Modifier.fillMaxWidth().weight(1f)) {
                                    Text(
                                        text = input.title,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Medium,
                                    )

                                    Text(
                                        text = input.description,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }

                                Spacer(Modifier.width(16.dp))

                                Switch(
                                    modifier = Modifier.focusRequester(focusRequester),
                                    checked = input.value,
                                    onCheckedChange = { onIntent(Intent.CheckInput(input.id, it)) }
                                )
                            }
                        }

                        is PathInput -> {
                            InputHeader(input.title, input.description)

                            Row(
                                Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        1.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    .clickable {
                                        onIntent(Intent.PathInput(input))
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(if (input.selectFolder) Res.drawable.folder else Res.drawable.file),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )

                                Spacer(Modifier.width(16.dp))

                                Text(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    text = if (input.value == null) "Select a ${if (input.selectFolder) "folder" else "file"}" else input.value.toString(),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )

                                Spacer(Modifier.width(16.dp))

                                if (input.value != null) {
                                    Box(
                                        Modifier.clip(CircleShape)
                                            .clickable {
                                                onIntent(Intent.PathClear(input.id))
                                            }
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            painter = painterResource(Res.drawable.trash),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onBackground,
                                        )
                                    }
                                }
                            }
                        }

                        is SelectInput -> {
                            var expanded by remember { mutableStateOf(false) }

                            InputHeader(input.title, input.description)

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it },
                            ) {
                                Column {
                                    Row(
                                        Modifier.fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                1.dp,
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            .clickable {
                                                expanded = !expanded
                                            }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            modifier = Modifier.fillMaxWidth().weight(1f),
                                            text = input.options.first { it.id == input.value }.text,
                                        )

                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            painter = painterResource(Res.drawable.chevron_down),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onBackground,
                                        )
                                    }

                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                    ) {
                                        input.options.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(text = option.text) },
                                                onClick = {
                                                    onIntent(Intent.SelectInput(input.id, option.id))
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = { onIntent(Intent.SubmitFormClick) },
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp, end = 16.dp, start = 16.dp)
            ) {
                Text(
                    text = state.form.buttonText
                )
            }
        }
    }
}

@Composable
fun InputHeader(title: String, description: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Medium,
    )

    Text(
        text = description,
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(Modifier.height(4.dp))
}

@Composable
@Preview
fun FormScreenPreview() {
    FormScreen(
        State(
            form = Form(
                text = "",
                pluginId = "",
                title = "A Random Form",
                buttonText = "Fill",
                inputs = listOf(
                    TextInput(
                        id = "id",
                        title = "Name",
                        description = "Type your name",
                        value = "meowmeow"
                    ),
                    NumberInput(
                        id = "id",
                        title = "Age",
                        description = "Type your age",
                        value = 25
                    ),
                    CheckInput(
                        id = "id",
                        title = "Cats",
                        description = "Do you like cats?",
                        value = true
                    ),
                    PathInput(
                        id = "id",
                        title = "Images",
                        description = "Select some image",
                        value = null
                    ),
                    PathInput(
                        id = "id",
                        title = "Images",
                        description = "Select some image",
                        value = "/home/lighttigerxiv/Pictures/picture.png"
                    ),
                    PathInput(
                        id = "id",
                        title = "Images",
                        description = "Select some image",
                        selectFolder = true
                    ),
                    PathInput(
                        id = "id",
                        title = "Images",
                        description = "Select some image",
                        value = "/home/lighttigerxiv/Pictures",
                        selectFolder = true
                    ),
                    SelectInput(
                        id = "id",
                        title = "Pokemons",
                        description = "Select some pokemon",
                        value = "oshawott",
                        options = listOf(
                            SelectOption(
                                id = "bulbasaur",
                                text = "Bulbasaur",
                            ),
                            SelectOption(
                                id = "charmander",
                                text = "charmander",
                            ),
                            SelectOption(
                                id = "squirtle",
                                text = "Squirtle",
                            ),
                            SelectOption(
                                id = "oshawott",
                                text = "Oshawott",
                            ),
                            SelectOption(
                                id = "snivy",
                                text = "Snivy"
                            ),
                            SelectOption(
                                id = "tepig",
                                text = "Tepig",
                            )
                        ),
                    )
                ),
                customInfo = listOf(),
            )
        )
    ) {}
}
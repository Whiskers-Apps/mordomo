package org.whiskersapps.mordomo.ui.form_screen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.CheckFormResult
import lib.CheckInput
import lib.FormResults
import lib.NumberFormResult
import lib.NumberInput
import lib.PathFormResult
import lib.PathInput
import lib.SelectFormResult
import lib.SelectInput
import lib.TextFormResult
import lib.TextInput
import org.whiskersapps.mordomo.core.features.form.FormRepository
import org.whiskersapps.mordomo.core.features.socket.SocketRepository
import org.whiskersapps.mordomo.core.features.window.WindowRepository
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import org.whiskersapps.mordomo.ui.form_screen.FormScreenIntent as Intent

class FormScreenVM(
    private val formRepository: FormRepository,
    private val socketRepository: SocketRepository,
    private val windowRepository: WindowRepository
) {
    private val _state = MutableStateFlow(FormScreenState())
    val state = _state.asStateFlow()

    private val scope = CoroutineScope(IO)

    init {
        formRepository.form.onEach { form ->
            if (form == null) return@onEach

            _state.update { it.copy(form = form) }
        }.launchIn(CoroutineScope(IO))
    }

    fun onIntent(intent: Intent) {
        when (intent) {
            Intent.Back -> {
                scope.launch {
                    formRepository.setForm(null)
                    windowRepository.goToMain()
                }
            }

            is Intent.TextInput -> {
                scope.launch {
                    val inputs = state.value.form.inputs.map { input ->
                        when (input) {
                            is TextInput -> {
                                if (input.id == intent.id)
                                    input.copy(value = intent.text)
                                else
                                    input
                            }

                            else -> input
                        }
                    }

                    _state.update { it.copy(form = it.form.copy(inputs = inputs)) }
                }
            }

            is Intent.NumberInput -> {
                scope.launch {
                    val inputs = state.value.form.inputs.map { input ->
                        when (input) {
                            is NumberInput -> {
                                if (input.id == intent.id)
                                    input.copy(value = intent.text.toIntOrNull() ?: 0)
                                else
                                    input
                            }

                            else -> input
                        }
                    }

                    _state.update { it.copy(form = it.form.copy(inputs = inputs)) }
                }
            }

            is Intent.CheckInput -> {
                scope.launch {
                    val inputs = state.value.form.inputs.map { input ->
                        when (input) {
                            is CheckInput -> {
                                if (input.id == intent.id)
                                    input.copy(value = intent.checked)
                                else
                                    input
                            }

                            else -> input
                        }
                    }

                    _state.update { it.copy(form = it.form.copy(inputs = inputs)) }
                }
            }

            is Intent.SelectInput -> {
                val inputs = state.value.form.inputs.map { input ->
                    when (input) {
                        is SelectInput -> {
                            if (input.id == intent.id)
                                input.copy(value = intent.optionId)
                            else
                                input
                        }

                        else -> input
                    }
                }

                _state.update { it.copy(form = it.form.copy(inputs = inputs)) }
            }

            is Intent.PathClear -> {
                val inputs = state.value.form.inputs.map { input ->
                    when (input) {
                        is PathInput -> {
                            if (input.id == intent.id)
                                input.copy(value = null)
                            else
                                input
                        }

                        else -> input
                    }
                }

                _state.update { it.copy(form = it.form.copy(inputs = inputs)) }
            }

            is Intent.PathInput -> {
                val chooser = JFileChooser()

                if (intent.input.value != null) {
                    chooser.currentDirectory = if (intent.input.selectFolder) {
                        File(intent.input.value!!)
                    } else {
                        File(intent.input.value!!).parentFile
                    }
                }

                if (intent.input.selectFolder) {
                    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                }

                if (intent.input.fileExtensions.isNotEmpty()) {
                    chooser.fileFilter =
                        FileNameExtensionFilter("Supported Types", *intent.input.fileExtensions.toTypedArray())
                }

                val result = chooser.showOpenDialog(null)

                if (result == JFileChooser.APPROVE_OPTION) {
                    if (chooser.selectedFile == null) return

                    val inputs = state.value.form.inputs.map { input ->
                        when (input) {
                            is PathInput -> {
                                if (input.id == intent.input.id)
                                    input.copy(value = chooser.selectedFile.absolutePath)
                                else
                                    input
                            }

                            else -> input
                        }
                    }

                    _state.update { it.copy(form = it.form.copy(inputs = inputs)) }
                }
            }

            Intent.SubmitFormClick -> onSubmitFormClick()
        }
    }

    private fun onSubmitFormClick() {
        scope.launch {
            val form = state.value.form
            val results = form.inputs.map { input ->
                when (input) {
                    is CheckInput -> CheckFormResult(id = input.id, value = input.value, customInfo = input.customInfo)
                    is NumberInput -> NumberFormResult(
                        id = input.id,
                        value = input.value,
                        customInfo = input.customInfo
                    )

                    is PathInput -> PathFormResult(
                        id = input.id,
                        value = input.value,
                        customInfo = input.customInfo
                    )

                    is SelectInput -> SelectFormResult(
                        id = input.id,
                        value = input.value,
                        customInfo = input.customInfo
                    )

                    is TextInput -> TextFormResult(
                        id = input.id,
                        value = input.value,
                        customInfo = input.customInfo
                    )
                }
            }

            launch {
                socketRepository.sendToPlugin(
                    form.pluginId,
                    FormResults(results = results, customInfo = form.customInfo)
                )
            }

            formRepository.setForm(null)

            windowRepository.goToMain()
            windowRepository.hide()
        }
    }
}
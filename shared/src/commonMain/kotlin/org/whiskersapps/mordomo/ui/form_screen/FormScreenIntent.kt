package org.whiskersapps.mordomo.ui.form_screen

sealed interface FormScreenIntent {
    data object Back: FormScreenIntent
    data class TextInput(val id: String, val text: String) : FormScreenIntent
    data class NumberInput(val id: String, val text: String) : FormScreenIntent
    data class CheckInput(val id: String, val checked: Boolean) : FormScreenIntent
    data class SelectInput(val id: String, val optionId: String): FormScreenIntent
    data class PathInput(val input: lib.PathInput): FormScreenIntent
    data class PathClear(val id: String): FormScreenIntent
    data object SubmitFormClick: FormScreenIntent
}
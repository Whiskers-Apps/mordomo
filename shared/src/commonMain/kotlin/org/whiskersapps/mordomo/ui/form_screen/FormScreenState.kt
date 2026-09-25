package org.whiskersapps.mordomo.ui.form_screen

import lib.Form

data class FormScreenState(
    val form: Form = Form(
        pluginId = "",
        title = "",
        buttonText = "",
        inputs = listOf(),
        customInfo = listOf(),
    )
)
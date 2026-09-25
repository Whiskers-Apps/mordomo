package org.whiskersapps.mordomo.ui.form_screen

import lib.Form
import lib.FormInput

data class State(
    val form: Form = Form(
        text = "",
        pluginId = "",
        title = "",
        buttonText = "",
        inputs = listOf(),
        customInfo = listOf(),
    )
)
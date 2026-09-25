package org.whiskersapps.mordomo.core.features.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import lib.Form

class FormRepository {
    private val _form = MutableStateFlow<Form?>(null)
    val form = _form.asStateFlow()

    fun setForm(form: Form?){
        _form.update { form }
    }
}
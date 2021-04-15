package fr.dcproject.common.validation

import io.konform.validation.ValidationBuilder
import java.net.MalformedURLException
import java.net.URL

fun ValidationBuilder<String>.isUrl() =
    addConstraint("is not url") {
        try {
            val url = URL(it)
            true
        } catch (e: MalformedURLException) {
            false
        }
    }

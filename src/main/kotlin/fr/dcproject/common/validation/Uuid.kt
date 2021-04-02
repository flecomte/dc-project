package fr.dcproject.common.validation

import io.konform.validation.ValidationBuilder
import java.util.UUID

fun ValidationBuilder<String>.isUuid() =
    addConstraint("must be UUID") {
        try {
            UUID.fromString(it)
            true
        } catch (exception: IllegalArgumentException) {
            false
        }
    }

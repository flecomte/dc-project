package fr.dcproject.common.validation

import io.konform.validation.ValidationBuilder
import io.konform.validation.jsonschema.pattern

fun ValidationBuilder<String>.email() = pattern(""".+@.+\..+""")

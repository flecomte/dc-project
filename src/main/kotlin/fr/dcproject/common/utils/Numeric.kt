package fr.dcproject.common.utils

fun String.isInt(): Boolean = this.toIntOrNull() != null
fun String.isBool(): Boolean = this == "true" || this == "false"

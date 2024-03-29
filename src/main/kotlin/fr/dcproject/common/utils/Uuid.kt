package fr.dcproject.common.utils

import java.util.UUID

fun String.toUUID(): UUID = UUID.fromString(this.trim())

fun List<String?>.toUUID(): List<UUID> = this
    .filterNotNull()
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .map { UUID.fromString(it) }

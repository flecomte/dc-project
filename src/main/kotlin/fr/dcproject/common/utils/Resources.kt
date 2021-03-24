package fr.dcproject.common.utils

import java.net.URL

fun String.readResource(callback: (String) -> Unit = {}): String {
    val content = callback::class.java.getResource(this)?.readText() ?: error("File not found")
    callback(content)
    return content
}

fun String.getResource(callback: (URL) -> Unit = {}): URL {
    val content = callback::class.java.getResource(this) ?: error("File not found")
    callback(content)
    return content
}

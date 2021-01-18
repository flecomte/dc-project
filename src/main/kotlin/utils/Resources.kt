package fr.dcproject.utils

fun String.readResource(callback: (String) -> Unit = {}): String {
    val content = callback::class.java.getResource(this).readText()
    callback(content)
    return content
}

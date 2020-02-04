package fr.dcproject.utils

fun String.readResource(callbak: (String) -> Unit = {}): String {
    val content = callbak::class.java.getResource(this).readText()
    callbak(content)
    return content
}
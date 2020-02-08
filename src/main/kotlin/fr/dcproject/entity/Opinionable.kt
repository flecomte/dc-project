package fr.dcproject.entity

interface Opinionable {
    val opinions: MutableMap<String, Int>
}

class OpinionableImp : Opinionable {
    override val opinions: MutableMap<String, Int> = mutableMapOf()
}
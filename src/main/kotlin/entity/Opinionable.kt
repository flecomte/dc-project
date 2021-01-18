package fr.dcproject.entity

typealias Opinions = Map<String, Int>
typealias OpinionsMutable = MutableMap<String, Int>

interface Opinionable {
    val opinions: Opinions
}

class OpinionableImp : Opinionable {
    override var opinions: OpinionsMutable = mutableMapOf()
}

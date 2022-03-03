package fr.dcproject.component.opinion.entity

typealias Opinions = Map<String, Int>
typealias OpinionsMutable = MutableMap<String, Int>

interface Opinionable {
    val opinions: Opinions
}

data class OpinionableImp(override var opinions: OpinionsMutable = mutableMapOf()) : Opinionable

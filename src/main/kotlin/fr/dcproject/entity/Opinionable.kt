package fr.dcproject.entity

import fr.postgresjson.entity.EntityI

class OpinionAggregation(
    override val entries: Set<Map.Entry<String, Int>> = emptySet()
) : AbstractMap<String, Int>(), EntityI

interface Opinionable {
    val opinions: MutableMap<String, Int>
}

class OpinionableImp : Opinionable {
    override val opinions: MutableMap<String, Int> = mutableMapOf()
}
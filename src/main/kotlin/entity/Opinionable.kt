package fr.dcproject.entity

import fr.postgresjson.entity.EntityI

class OpinionAggregation(
    private val underlying: MutableMap<String, Any> = mutableMapOf()
) : MutableMap<String, Any> by underlying, EntityI

interface Opinionable {
    var opinions: MutableMap<String, Int>
}

class OpinionableImp : Opinionable {
    override var opinions: MutableMap<String, Int> = mutableMapOf()
}
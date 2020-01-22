package fr.dcproject.entity

import fr.postgresjson.entity.EntityI
import fr.postgresjson.entity.mutable.EntityUpdatedAt
import fr.postgresjson.entity.mutable.EntityUpdatedAtImp

open class VoteAggregation(
    val up: Int,
    val neutral: Int,
    val down: Int,
    val total: Int,
    val score: Int
) : EntityI,
    EntityUpdatedAt by EntityUpdatedAtImp() {
    constructor() : this(0, 0, 0, 0, 0)
}

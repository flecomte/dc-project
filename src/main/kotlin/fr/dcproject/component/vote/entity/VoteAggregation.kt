package fr.dcproject.component.vote.entity

import fr.postgresjson.entity.EntityI
import fr.postgresjson.entity.EntityUpdatedAt
import fr.postgresjson.entity.EntityUpdatedAtImp

class VoteAggregation(
    val up: Int,
    val neutral: Int,
    val down: Int,
    val total: Int,
    val score: Int
) : EntityI,
    EntityUpdatedAt by EntityUpdatedAtImp() {
    constructor() : this(0, 0, 0, 0, 0)
}

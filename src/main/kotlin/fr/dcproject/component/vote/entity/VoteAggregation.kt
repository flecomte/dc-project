package fr.dcproject.component.vote.entity

import fr.dcproject.common.entity.UpdatedAt

class VoteAggregation(
    val up: Int,
    val neutral: Int,
    val down: Int,
    val total: Int,
    val score: Int
) : fr.postgresjson.entity.EntityI,
    UpdatedAt by UpdatedAt.Imp() {
    constructor() : this(0, 0, 0, 0, 0)
}

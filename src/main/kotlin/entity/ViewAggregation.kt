package fr.dcproject.entity

import fr.postgresjson.entity.EntityI
import fr.postgresjson.entity.immutable.EntityUpdatedAt
import fr.postgresjson.entity.immutable.EntityUpdatedAtImp

open class ViewAggregation(
    val total: Int,
    val unique: Int
) : EntityI,
    EntityUpdatedAt by EntityUpdatedAtImp() {
    constructor() : this(0, 0)
}

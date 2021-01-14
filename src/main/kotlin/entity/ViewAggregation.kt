package fr.dcproject.entity

import fr.postgresjson.entity.EntityI
import fr.postgresjson.entity.EntityUpdatedAt
import fr.postgresjson.entity.EntityUpdatedAtImp

class ViewAggregation(
    val total: Int,
    val unique: Int
) : EntityI,
    EntityUpdatedAt by EntityUpdatedAtImp() {
    constructor() : this(0, 0)
}

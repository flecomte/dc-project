package fr.dcproject.component.views.entity

import fr.dcproject.common.entity.UpdatedAt
import fr.postgresjson.entity.EntityI

class ViewAggregation(
    val total: Int,
    val unique: Int
) : EntityI,
    UpdatedAt by UpdatedAt.Imp() {
    constructor() : this(0, 0)
}

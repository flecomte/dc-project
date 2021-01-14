package fr.dcproject.dto

import fr.dcproject.entity.ViewAggregation

class ViewAggregation(
    val total: Int,
    val unique: Int
) {
    constructor(views: ViewAggregation) : this(views.total, views.unique)
}

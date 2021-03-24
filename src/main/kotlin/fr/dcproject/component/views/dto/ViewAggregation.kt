package fr.dcproject.component.views.dto

import fr.dcproject.component.views.entity.ViewAggregation

class ViewAggregation(
    val total: Int,
    val unique: Int
) {
    constructor(views: ViewAggregation) : this(views.total, views.unique)
}

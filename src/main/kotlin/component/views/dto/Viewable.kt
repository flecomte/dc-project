package fr.dcproject.component.views.dto

interface Viewable {
    var views: ViewAggregation

    class Imp(views: fr.dcproject.component.views.entity.ViewAggregation) : Viewable {
        override var views: ViewAggregation = ViewAggregation(views.total, views.unique)
    }
}

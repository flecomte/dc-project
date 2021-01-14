package fr.dcproject.dto

interface Viewable {
    var views: ViewAggregation

    class Imp(views: fr.dcproject.entity.ViewAggregation) : Viewable {
        override var views: ViewAggregation = ViewAggregation(views.total, views.unique)
    }
}

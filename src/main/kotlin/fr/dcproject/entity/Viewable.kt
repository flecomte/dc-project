package fr.dcproject.entity

interface Viewable {
    var views: ViewAggregation
}

class ViewableImp : Viewable {
    override var views: ViewAggregation = ViewAggregation()
}
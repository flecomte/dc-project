package fr.dcproject.views

import fr.dcproject.entity.CitizenRef
import fr.dcproject.entity.ViewAggregation
import org.elasticsearch.client.Response
import org.joda.time.DateTime

interface ViewManager <T> {
    fun addView(ip: String, entity: T, citizen: CitizenRef? = null, dateTime: DateTime = DateTime.now()): Response?
    fun getViewsCount(entity: T): ViewAggregation
}
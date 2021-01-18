package fr.dcproject.component.views

import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.entity.ViewAggregation
import org.elasticsearch.client.Response
import org.joda.time.DateTime

interface ViewManager <T> {
    /**
     * Add view to one entity
     */
    fun addView(ip: String, entity: T, citizen: CitizenI? = null, dateTime: DateTime = DateTime.now()): Response?

    /**
     * Get Views aggregations
     */
    fun getViewsCount(entity: T): ViewAggregation
}
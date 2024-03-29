package fr.dcproject.component.article.database

import fr.dcproject.common.entity.VersionableId
import fr.dcproject.common.utils.contentToString
import fr.dcproject.common.utils.getJsonField
import fr.dcproject.common.utils.toIso
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.views.ViewRepository
import fr.dcproject.component.views.entity.ViewAggregation
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestClient
import org.joda.time.DateTime
import java.util.UUID

/**
 * Wrapper for manage views with elasticsearch
 */
class ArticleViewRepository <A> (private val restClient: RestClient) : ViewRepository<A> where A : VersionableId, A : ArticleI {
    /**
     * Add view on article to elasticsearch
     */
    override fun addView(ip: String, entity: A, citizen: CitizenI?, dateTime: DateTime) {
        val isLogged = (citizen != null).toString()
        val ref = citizen?.id ?: UUID.nameUUIDFromBytes(ip.toByteArray())!!
        val request = Request(
            "POST",
            "/views/_doc/"
        ).apply {
            //language=JSON
            setJsonEntity(
                """
                {
                  "logged": $isLogged,
                  "type": "article",
                  "user_ref": "$ref",
                  "ip": "$ip",
                  "id": "${entity.id}",
                  "version_id": "${entity.versionId}",
                  "citizen_id": "${citizen?.id}",
                  "view_at": "${dateTime.toIso()}"
                }
                """.trimIndent()
            )
        }

        restClient.performRequest(request)
    }

    /**
     * Get article views aggregations from elasticsearch
     */
    override fun getViewsCount(entity: A): ViewAggregation {
        val request = Request(
            "GET",
            "/views/_search"
        ).apply {
            //language=JSON
            setJsonEntity(
                """
                {
                  "size": 0,
                  "query": {
                    "bool": {
                      "must": {
                        "term": {
                          "version_id": "${entity.versionId}"
                        }
                      }
                    }
                  },
                  "aggs" : {
                    "total": {
                      "composite" : {
                        "sources" : [
                          { "version_id": { "terms": {"field": "version_id" } } }
                        ]
                      }
                    },
                    "unique" : {
                      "cardinality" : {
                        "field" : "user_ref",
                        "precision_threshold": 1
                      }
                    }
                  }
                }
                """.trimIndent()
            )
        }

        return restClient
            .performRequest(request).contentToString().run {
                ViewAggregation(
                    getJsonField("$.aggregations.total.buckets[0].doc_count") ?: 0,
                    getJsonField("$.aggregations.unique.value") ?: 0
                )
            }
    }
}

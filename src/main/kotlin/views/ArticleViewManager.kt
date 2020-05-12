package fr.dcproject.views

import fr.dcproject.entity.*
import fr.dcproject.utils.contentToString
import fr.dcproject.utils.getJsonField
import fr.dcproject.utils.toIso
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.elasticsearch.client.RestClient
import org.joda.time.DateTime
import java.util.*

class ArticleViewManager(private val restClient: RestClient) : ViewManager<ArticleRefVersioning> {
    override fun addView(ip: String, article: ArticleRefVersioning, citizen: CitizenRef?, dateTime: DateTime): Response? {
        val isLogged = (citizen != null).toString()
        val ref = citizen?.id ?: UUID.nameUUIDFromBytes(ip.toByteArray())!!
        val request = Request(
            "POST",
            "/views/_doc/"
        ).apply {
            //language=JSON
            setJsonEntity("""
                {
                  "logged": $isLogged,
                  "type": "article",
                  "user_ref": "$ref",
                  "ip": "$ip",
                  "id": "${article.id}",
                  "version_id": "${article.versionId}",
                  "citizen_id": "${citizen?.id}",
                  "view_at": "${dateTime.toIso()}"
                }
            """.trimIndent())
        }

        return restClient.performRequest(request)
    }

    override fun getViewsCount(article: ArticleRefVersioning): ViewAggregation {
        val request = Request(
            "GET",
            "/views/_search"
        ).apply {
            //language=JSON
            setJsonEntity("""
            {
              "size": 0,
              "query": {
                "bool": {
                  "must": {
                    "term": {
                      "version_id": "${article.versionId}"
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
            """.trimIndent())
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
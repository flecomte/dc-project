package fr.dcproject.component.views

import fr.dcproject.common.utils.waitElasticsearchIsUp
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestClient

fun RestClient.createEsIndexForViews() {
    waitElasticsearchIsUp()

    /* Create index if not exist */
    if (performRequest(Request("HEAD", "/views?include_type_name=false")).statusLine.statusCode == 404) {
        Request(
            "PUT",
            "/views?include_type_name=false"
        ).apply {
            //language=JSON
            setJsonEntity(
                """
        {
          "settings": {
            "number_of_shards": 5
          },
          "mappings": {
            "properties": {
              "logged": {
                "type": "boolean"
              },
              "type": {
                "type": "keyword"
              },
              "user_ref": {
                "type": "keyword"
              },
              "id": {
                "type": "keyword"
              },
              "version_id": {
                "type": "keyword"
              },
              "ip": {
                "type": "keyword"
              },
              "citizen_id": {
                "type": "keyword"
              },
              "view_at": {
                "type": "date"
              }
            }
          }
        }
                """.trimIndent()
            )
        }.let {
            performRequest(it)
        }
    }
}

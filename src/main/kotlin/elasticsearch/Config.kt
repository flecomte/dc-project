package fr.dcproject.elasticsearch

import org.elasticsearch.client.Request
import org.elasticsearch.client.RestClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun waitElasticsearchIsUp(client: RestClient) {
    val logger: Logger = LoggerFactory.getLogger("fr.dcproject.elasticsearch")
    val request = Request("GET", "/_cluster/health")
    repeat(5*60 / 2) { // 5 minutes
        runCatching {
            client.performRequest(request).statusLine.statusCode
        }.onSuccess {
            if (it == 200) {
                logger.debug("Elasticsearch is Ready! Continue...")
                return
            } else {
                logger.debug("sleep 2s and retry...")
                Thread.sleep(2000)
            }
        }.onFailure {
            logger.debug("${it.message}, sleep 2s and retry...")
            Thread.sleep(2000)
        }
    }
    error("Elasticsearch is not ready")
}

fun configElasticIndexes(client: RestClient) {
    waitElasticsearchIsUp(client)

    /* Create index if not exist */
    client.run {
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
}
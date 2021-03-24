package fr.dcproject.common.utils

import org.elasticsearch.client.Request
import org.elasticsearch.client.RestClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun RestClient.waitElasticsearchIsUp() {
    val logger: Logger = LoggerFactory.getLogger("fr.dcproject.elasticsearch")
    val request = Request("GET", "/_cluster/health")
    repeat(5 * 60 / 2) { // 5 minutes
        runCatching {
            performRequest(request).statusLine.statusCode
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

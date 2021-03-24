package fr.dcproject.common.utils

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import org.apache.http.util.EntityUtils
import org.elasticsearch.client.Response
import org.slf4j.LoggerFactory

fun Response.contentToString(): String {
    return EntityUtils.toString(this.entity)
}

fun Response.getField(jsonPath: String): Int? {
    return try {
        JsonPath.read(this.contentToString(), jsonPath)
    } catch (e: PathNotFoundException) {
        null
    }
}

fun String.getJsonField(jsonPath: String): Int? {
    return try {
        JsonPath.read(this, jsonPath)
    } catch (e: PathNotFoundException) {
        LoggerFactory.getLogger("fr.dcproject.utils.getJsonField")
            .warn("No value for Json path ${JsonPath.compile(jsonPath).path}")
        null
    }
}

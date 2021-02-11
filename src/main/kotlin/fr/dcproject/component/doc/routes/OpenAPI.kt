package fr.dcproject.component.doc.routes

import fr.dcproject.common.utils.readResource
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
fun Route.definition() {
    get("/") {
        call.respondText("/openapi.yaml".readResource(), ContentType("text", "yaml"))
    }
}

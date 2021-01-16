package fr.dcproject.routes

import fr.dcproject.utils.readResource
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
fun Route.definition() {
    get("/") {
        call.respondText("/openapi.yaml".readResource(), ContentType("text", "yaml"))
    }
}

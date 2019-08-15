package fr.dcproject.routes

import Paths
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.repository.Citizen as CitizenRepository

@KtorExperimentalLocationsAPI
fun Route.citizen(repo: CitizenRepository) {
    get<Paths.CitizensRequest> {
        val citizens = repo.find(it.page, it.limit, it.sort, it.direction, it.search)
        call.respond(citizens)
    }

    get<Paths.CitizenRequest> {
        call.respond(it.citizen)
    }
}
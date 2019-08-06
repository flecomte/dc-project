package fr.dcproject.routes

import Paths
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.entity.Constitution as ConstitutionEntity
import fr.dcproject.repository.Constitution as ConstitutionRepository

@KtorExperimentalLocationsAPI
fun Route.constitution(repo: ConstitutionRepository) {
    get<Paths.ConstitutionsRequest> {
        val constitutions = repo.find(it.page, it.limit, it.sort, it.direction, it.search)
        call.respond(constitutions)
    }

    get<Paths.ConstitutionRequest> {
        call.respond(it.constitution)
    }

    post<Paths.PostConstitutionRequest>() {
        val constitution = call.receive<ConstitutionEntity>()
        repo.upsert(constitution)
        call.respond(constitution)
    }
}
package fr.dcproject.routes

import fr.postgresjson.repository.RepositoryI
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.entity.Constitution as ConstitutionEntity
import fr.dcproject.repository.Constitution as ConstitutionRepository

@KtorExperimentalLocationsAPI
object ConstitutionPaths {
    @Location("/constitutions") class ConstitutionsRequest(page: Int = 1, limit: Int = 50, val sort: String? = null, val direction: RepositoryI.Direction? = null, val search: String? = null) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
    }
    @Location("/constitutions/{constitution}") class ConstitutionRequest(val constitution: ConstitutionEntity)
    @Location("/constitutions/{constitution}/follow") class ConstitutionFollowRequest(val constitution: ConstitutionEntity)
    @Location("/constitutions") class PostConstitutionRequest
}

@KtorExperimentalLocationsAPI
fun Route.constitution(repo: ConstitutionRepository) {
    get<ConstitutionPaths.ConstitutionsRequest> {
        val constitutions = repo.find(it.page, it.limit, it.sort, it.direction, it.search)
        call.respond(constitutions)
    }

    get<ConstitutionPaths.ConstitutionRequest> {
        call.respond(it.constitution)
    }

    post<ConstitutionPaths.PostConstitutionRequest>() {
        val constitution = call.receive<ConstitutionEntity>()
        repo.upsert(constitution)
        call.respond(constitution)
    }
}
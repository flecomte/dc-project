package fr.dcproject.component.follow.routes.constitution

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.constitution.ConstitutionRef
import fr.dcproject.component.follow.FollowAccessControl
import fr.dcproject.component.follow.FollowConstitutionRepository
import fr.dcproject.security.assert
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object GetFollowConstitution {
    @Location("/constitutions/{constitution}/follows")
    class ConstitutionFollowRequest(val constitution: ConstitutionRef)

    fun Route.getFollowConstitution(repo: FollowConstitutionRepository, ac: FollowAccessControl) {
        get<ConstitutionFollowRequest> {
            repo.findFollow(citizen, it.constitution)?.let { follow ->
                ac.assert { canView(follow, citizenOrNull) }
                call.respond(follow)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}

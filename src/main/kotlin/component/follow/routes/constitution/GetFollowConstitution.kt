package fr.dcproject.component.follow.routes.constitution

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.follow.FollowConstitutionRepository
import fr.dcproject.component.follow.FollowVoter
import fr.dcproject.entity.ConstitutionRef
import fr.dcproject.voter.assert
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

    fun Route.getFollowConstitution(repo: FollowConstitutionRepository, voter: FollowVoter) {
        get<ConstitutionFollowRequest> {
            repo.findFollow(citizen, it.constitution)?.let { follow ->
                voter.assert { canView(follow, citizenOrNull) }
                call.respond(follow)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}

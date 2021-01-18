package fr.dcproject.component.follow.routes.constitution

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.follow.FollowConstitutionRepository
import fr.dcproject.component.follow.FollowForUpdate
import fr.dcproject.component.follow.FollowVoter
import fr.dcproject.entity.ConstitutionRef
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.delete
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object UnfollowConstitution {
    @Location("/constitutions/{constitution}/follows")
    class ConstitutionUnfollowRequest(val constitution: ConstitutionRef)

    fun Route.unfollowConstitution(repo: FollowConstitutionRepository, voter: FollowVoter) {
        delete<ConstitutionUnfollowRequest> {
            val follow = FollowForUpdate(target = it.constitution, createdBy = this.citizen)
            voter.assert { canDelete(follow, citizenOrNull) }
            repo.unfollow(follow)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

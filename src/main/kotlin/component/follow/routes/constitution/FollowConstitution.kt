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
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object FollowConstitution {
    @Location("/constitutions/{constitution}/follows")
    class ConstitutionFollowRequest(val constitution: ConstitutionRef)

    fun Route.followConstitution(repo: FollowConstitutionRepository, voter: FollowVoter) {
        post<ConstitutionFollowRequest> {
            val follow = FollowForUpdate(target = it.constitution, createdBy = this.citizen)
            voter.assert { canCreate(follow, citizenOrNull) }
            repo.follow(follow)
            call.respond(HttpStatusCode.Created)
        }
    }
}

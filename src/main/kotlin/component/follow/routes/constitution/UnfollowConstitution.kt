package fr.dcproject.component.follow.routes.constitution

import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.constitution.ConstitutionRef
import fr.dcproject.component.follow.FollowAccessControl
import fr.dcproject.component.follow.FollowConstitutionRepository
import fr.dcproject.component.follow.FollowForUpdate
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.delete
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object UnfollowConstitution {
    @Location("/constitutions/{constitution}/follows")
    class ConstitutionUnfollowRequest(constitution: UUID) {
        val constitution = ConstitutionRef(constitution)
    }

    fun Route.unfollowConstitution(repo: FollowConstitutionRepository, ac: FollowAccessControl) {
        delete<ConstitutionUnfollowRequest> {
            val follow = FollowForUpdate(target = it.constitution, createdBy = this.citizen)
            ac.assert { canDelete(follow, citizenOrNull) }
            repo.unfollow(follow)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

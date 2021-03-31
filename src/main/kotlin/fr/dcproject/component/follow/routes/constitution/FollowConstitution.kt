package fr.dcproject.component.follow.routes.constitution

import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.constitution.database.ConstitutionRef
import fr.dcproject.component.follow.FollowAccessControl
import fr.dcproject.component.follow.database.FollowConstitutionRepository
import fr.dcproject.component.follow.database.FollowForUpdate
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object FollowConstitution {
    @Location("/constitutions/{constitution}/follows")
    class ConstitutionFollowRequest(constitution: UUID) {
        val constitution = ConstitutionRef(constitution)
    }

    fun Route.followConstitution(repo: FollowConstitutionRepository, ac: FollowAccessControl) {
        post<ConstitutionFollowRequest> {
            mustBeAuth()
            val follow = FollowForUpdate(target = it.constitution, createdBy = this.citizen)
            ac.assert { canCreate(follow, citizenOrNull) }
            repo.follow(follow)
            call.respond(HttpStatusCode.Created)
        }
    }
}

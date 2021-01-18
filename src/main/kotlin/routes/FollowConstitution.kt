package fr.dcproject.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.entity.ConstitutionRef
import fr.dcproject.entity.FollowForUpdate
import fr.dcproject.security.voter.FollowVoter
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.repository.FollowConstitution as FollowConstitutionRepository

@KtorExperimentalLocationsAPI
object FollowConstitutionPaths {
    @Location("/constitutions/{constitution}/follows")
    class ConstitutionFollowRequest(val constitution: ConstitutionRef)

    @Location("/citizens/{citizen}/follows/constitutions")
    class CitizenFollowConstitutionRequest(val citizen: CitizenRef)
}

@KtorExperimentalLocationsAPI
fun Route.followConstitution(repo: FollowConstitutionRepository, voter: FollowVoter) {
    post<FollowConstitutionPaths.ConstitutionFollowRequest> {
        val follow = FollowForUpdate(target = it.constitution, createdBy = this.citizen)
        voter.assert { canCreate(follow, citizenOrNull) }
        repo.follow(follow)
        call.respond(HttpStatusCode.Created)
    }

    delete<FollowConstitutionPaths.ConstitutionFollowRequest> {
        val follow = FollowForUpdate(target = it.constitution, createdBy = this.citizen)
        voter.assert { canDelete(follow, citizenOrNull) }
        repo.unfollow(follow)
        call.respond(HttpStatusCode.NoContent)
    }

    get<FollowConstitutionPaths.ConstitutionFollowRequest> {
        repo.findFollow(citizen, it.constitution)?.let { follow ->
            voter.assert { canView(follow, citizenOrNull) }
            call.respond(follow)
        } ?: call.respond(HttpStatusCode.NotFound)
    }

    get<FollowConstitutionPaths.CitizenFollowConstitutionRequest> {
        val follows = repo.findByCitizen(it.citizen)
        voter.assert { canView(follows.result, citizenOrNull) }
        call.respond(follows)
    }
}

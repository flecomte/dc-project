package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.Citizen
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.*
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.entity.Constitution as ConstitutionEntity
import fr.dcproject.entity.Follow as FollowEntity
import fr.dcproject.repository.FollowConstitution as FollowConstitutionRepository

@KtorExperimentalLocationsAPI
object FollowConstitutionPaths {
    @Location("/constitutions/{constitution}/follow") class ConstitutionFollowRequest(val constitution: ConstitutionEntity)
    @Location("/citizens/{citizen}/follows/constitutions") class CitizenFollowConstitutionRequest(val citizen: Citizen)
}

@KtorExperimentalLocationsAPI
fun Route.followConstitution(repo: FollowConstitutionRepository) {
    post<FollowConstitutionPaths.ConstitutionFollowRequest> {
        val follow = FollowEntity(target = it.constitution, createdBy = this.citizen)
//        TODO create voter
//        assertCan(FollowVoter.Action.CREATE, follow)
        repo.follow(follow)
        call.respond(HttpStatusCode.Created)
    }

    delete<FollowConstitutionPaths.ConstitutionFollowRequest> {
        val follow = FollowEntity(target = it.constitution, createdBy = this.citizen)
//        TODO create voter
//        assertCan(FollowVoter.Action.DELETE, follow)
        repo.unfollow(follow)
        call.respond(HttpStatusCode.NoContent)
    }

    get<FollowConstitutionPaths.CitizenFollowConstitutionRequest> {
        val follows = repo.findByCitizen(it.citizen)
//        TODO create voter
//        assertCan(FollowVoter.Action.VIEW, follows)
        call.respond(follows)
    }
}

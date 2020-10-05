package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.CitizenRef
import fr.dcproject.entity.ConstitutionRef
import fr.dcproject.security.voter.FollowVoter.Action.CREATE
import fr.dcproject.security.voter.FollowVoter.Action.DELETE
import fr.dcproject.security.voter.FollowVoter.Action.VIEW
import fr.ktorVoter.assertCan
import fr.ktorVoter.assertCanAll
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import fr.dcproject.entity.Follow as FollowEntity
import fr.dcproject.repository.FollowConstitution as FollowConstitutionRepository

@KtorExperimentalLocationsAPI
object FollowConstitutionPaths {
    @Location("/constitutions/{constitution}/follows")
    class ConstitutionFollowRequest(val constitution: ConstitutionRef)

    @Location("/citizens/{citizen}/follows/constitutions")
    class CitizenFollowConstitutionRequest(val citizen: CitizenRef)
}

@KtorExperimentalLocationsAPI
fun Route.followConstitution(repo: FollowConstitutionRepository) {
    post<FollowConstitutionPaths.ConstitutionFollowRequest> {
        val follow = FollowEntity(target = it.constitution, createdBy = this.citizen)
        assertCan(CREATE, follow)
        repo.follow(follow)
        call.respond(HttpStatusCode.Created)
    }

    delete<FollowConstitutionPaths.ConstitutionFollowRequest> {
        val follow = FollowEntity(target = it.constitution, createdBy = this.citizen)
        assertCan(DELETE, follow)
        repo.unfollow(follow)
        call.respond(HttpStatusCode.NoContent)
    }

    get<FollowConstitutionPaths.ConstitutionFollowRequest> {
        repo.findFollow(citizen, it.constitution)?.let { follow ->
            assertCan(VIEW, follow)
            call.respond(follow)
        } ?: call.respond(HttpStatusCode.NotFound)
    }

    get<FollowConstitutionPaths.CitizenFollowConstitutionRequest> {
        val follows = repo.findByCitizen(it.citizen)
        assertCanAll(VIEW, follows.result)
        call.respond(follows)
    }
}

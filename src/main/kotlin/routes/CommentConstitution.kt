package fr.dcproject.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.comment.generic.CommentForUpdate
import fr.dcproject.component.comment.generic.CommentVoter
import fr.dcproject.entity.ConstitutionRef
import fr.dcproject.repository.CommentConstitutionRepository
import fr.dcproject.voter.assert
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object CommentConstitutionPaths {
    @Location("/constitutions/{constitution}/comments")
    class ConstitutionCommentRequest(val constitution: ConstitutionRef)

    @Location("/citizens/{citizen}/comments/constitutions")
    class CitizenCommentConstitutionRequest(val citizen: Citizen)
}

@KtorExperimentalLocationsAPI
fun Route.commentConstitution(repo: CommentConstitutionRepository, voter: CommentVoter) {
    get<CommentConstitutionPaths.ConstitutionCommentRequest> {
        val comments = repo.findByTarget(it.constitution)
        voter.assert { canView(comments.result, citizenOrNull) }
        call.respond(HttpStatusCode.OK, comments)
    }

    post<CommentConstitutionPaths.ConstitutionCommentRequest> {
        val content = call.receiveText()
        val comment = CommentForUpdate(
            target = it.constitution,
            createdBy = citizen,
            content = content
        )
        voter.assert { canCreate(comment, citizenOrNull) }
        repo.comment(comment)

        call.respond(HttpStatusCode.Created, comment)
    }

    get<CommentConstitutionPaths.CitizenCommentConstitutionRequest> {
        val comments = repo.findByCitizen(it.citizen)
        voter.assert { canView(comments.result, citizenOrNull) }
        call.respond(comments)
    }
}

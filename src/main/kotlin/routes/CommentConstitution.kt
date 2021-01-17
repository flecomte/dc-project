package fr.dcproject.routes

import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.comment.generic.CommentForUpdate
import fr.dcproject.component.comment.generic.CommentVoter
import fr.dcproject.entity.ConstitutionRef
import fr.dcproject.repository.CommentConstitutionRepository
import fr.dcproject.voter.assert
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

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
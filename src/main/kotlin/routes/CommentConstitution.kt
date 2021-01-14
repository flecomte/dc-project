package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.comment.generic.CommentForUpdate
import fr.dcproject.component.comment.generic.CommentVoter.Action.CREATE
import fr.dcproject.component.comment.generic.CommentVoter.Action.VIEW
import fr.dcproject.entity.ConstitutionRef
import fr.dcproject.repository.CommentConstitutionRepository
import fr.ktorVoter.assertCan
import fr.ktorVoter.assertCanAll
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
fun Route.commentConstitution(repo: CommentConstitutionRepository) {
    get<CommentConstitutionPaths.ConstitutionCommentRequest> {
        val comments = repo.findByTarget(it.constitution)
        assertCanAll(VIEW, comments.result)
        call.respond(HttpStatusCode.OK, comments)
    }

    post<CommentConstitutionPaths.ConstitutionCommentRequest> {
        val content = call.receiveText()
        val comment = CommentForUpdate(
            target = it.constitution,
            createdBy = citizen,
            content = content
        )
        assertCan(CREATE, comment)
        repo.comment(comment)

        call.respond(HttpStatusCode.Created, comment)
    }

    get<CommentConstitutionPaths.CitizenCommentConstitutionRequest> {
        val comments = repo.findByCitizen(it.citizen)
        assertCanAll(VIEW, comments.result)
        call.respond(comments)
    }
}
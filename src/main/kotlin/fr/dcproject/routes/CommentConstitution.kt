package fr.dcproject.routes

import fr.dcproject.citizen
import fr.dcproject.entity.Citizen
import fr.dcproject.security.voter.CommentVoter.Action.CREATE
import fr.dcproject.security.voter.CommentVoter.Action.VIEW
import fr.dcproject.security.voter.assertCan
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route
import fr.dcproject.entity.Comment as CommentEntity
import fr.dcproject.entity.Constitution as ConstitutionEntity
import fr.dcproject.repository.CommentConstitution as CommentConstitutionRepository

@KtorExperimentalLocationsAPI
object CommentConstitutionPaths {
    @Location("/constitutions/{constitution}/comments") class ConstitutionCommentRequest(val constitution: ConstitutionEntity)
    @Location("/citizens/{citizen}/comments/constitutions") class CitizenCommentConstitutionRequest(val citizen: Citizen)
}

@KtorExperimentalLocationsAPI
fun Route.commentConstitution(repo: CommentConstitutionRepository) {
    get<CommentConstitutionPaths.ConstitutionCommentRequest> {
        assertCan(VIEW, it.constitution)

        val comment = repo.findByTarget(it.constitution)

        call.respond(HttpStatusCode.OK, comment)
    }

    post<CommentConstitutionPaths.ConstitutionCommentRequest> {
        assertCan(CREATE, it.constitution)

        val content = call.receiveText()
        val comment = CommentEntity(
            target = it.constitution,
            createdBy = citizen,
            content = content
        )
        repo.comment(comment)

        call.respond(HttpStatusCode.Created, comment)
    }

    get<CommentConstitutionPaths.CitizenCommentConstitutionRequest> {
        val comments = repo.findByCitizen(it.citizen)
        call.respond(comments)
    }
}
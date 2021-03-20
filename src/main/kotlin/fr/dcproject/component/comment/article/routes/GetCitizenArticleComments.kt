package fr.dcproject.component.comment.article.routes

import fr.dcproject.common.dto.toOutput
import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.comment.article.database.CommentArticleRepository
import fr.dcproject.component.comment.generic.CommentAccessControl
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import org.joda.time.DateTime
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetCitizenArticleComments {
    @Location("/citizens/{citizen}/comments/articles")
    class CitizenCommentArticleRequest(citizen: UUID) {
        val citizen = CitizenRef(citizen)
    }

    fun Route.getCitizenArticleComments(repo: CommentArticleRepository, ac: CommentAccessControl) {
        get<CitizenCommentArticleRequest> {
            repo.findByCitizen(it.citizen).let { comments ->
                ac.assert { canView(comments.result, citizenOrNull) }
                call.respond(
                    HttpStatusCode.OK,
                    comments.toOutput { comment ->
                        object {
                            val id: UUID = comment.id
                            val content: String = comment.content
                            val childrenCount: Int = comment.childrenCount ?: 0
                            val createdAt: DateTime = comment.createdAt
                            val updatedAt: DateTime = comment.updatedAt
                            val parent: Any? = comment.parent?.let { p ->
                                object {
                                    val id: UUID = p.id
                                    val reference: String = p.reference
                                }
                            }
                            val target: Any = comment.target.let { t ->
                                object {
                                    val id: UUID = t.id
                                    val reference: String = t.reference
                                }
                            }
                            val createdBy: Any = comment.createdBy.toOutput()
                            val votes: Any = comment.votes.let { v ->
                                object {
                                    val up: Int = v.up
                                    val neutral: Int = v.neutral
                                    val down: Int = v.down
                                    val total: Int = v.total
                                    val score: Int = v.score
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

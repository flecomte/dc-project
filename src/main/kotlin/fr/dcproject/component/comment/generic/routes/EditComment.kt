package fr.dcproject.component.comment.generic.routes

import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.generic.database.CommentRef
import fr.dcproject.component.comment.generic.database.CommentRepository
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.response.respond
import io.ktor.routing.Route
import org.joda.time.DateTime
import java.util.UUID

@KtorExperimentalLocationsAPI
object EditComment {
    @Location("/comments/{comment}")
    class EditCommentRequest(comment: UUID) {
        val comment = CommentRef(comment)
        class Input(val content: String)
    }

    fun Route.editComment(repo: CommentRepository, ac: CommentAccessControl) {
        put<EditCommentRequest> {
            val comment = repo.findById(it.comment.id) ?: throw NotFoundException("Comment not found")
            ac.assert { canUpdate(comment, citizenOrNull) }

            comment.content = call.receiveOrBadRequest<EditCommentRequest.Input>().content
            repo.edit(comment)

            call.respond(
                HttpStatusCode.OK,
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
                    val createdBy: Any = comment.createdBy.let { c ->
                        object {
                            val id: UUID = c.id
                            val name: Any = c.name.let { n ->
                                object {
                                    val firstName: String = n.firstName
                                    val lastName: String = n.lastName
                                }
                            }
                            val user: Any = c.user.let { u ->
                                object {
                                    val username: String = u.username
                                }
                            }
                        }
                    }
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
            )
        }
    }
}

package fr.dcproject.component.comment.generic.routes

import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.generic.database.CommentRef
import fr.dcproject.component.comment.generic.database.CommentRepository
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import org.joda.time.DateTime
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetOneComment {
    @Location("/comments/{comment}")
    class CommentRequest(comment: UUID) {
        val comment = CommentRef(comment)
    }

    fun Route.getOneComment(repo: CommentRepository, ac: CommentAccessControl) {
        get<CommentRequest> {
            val comment = repo.findById(it.comment.id) ?: throw NotFoundException("Comment ${it.comment.id} not found")
            ac.assert { canView(comment, citizenOrNull) }

            call.respond(
                HttpStatusCode.OK,
                object {
                    val id: UUID = comment.id
                    val content: String = comment.content
                    val childrenCount: Int = comment.childrenCount ?: 0
                    val createdAt: DateTime = comment.createdAt
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
                    val votes: Any = object {
                        val up: Int = 0
                        val neutral: Int = 0
                        val down: Int = 0
                        val total: Int = 0
                        val score: Int = 0
                    }
                }
            )
        }
    }
}

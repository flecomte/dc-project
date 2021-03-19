package fr.dcproject.component.comment.constitution.routes

import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.constitution.database.CommentConstitutionRepository
import fr.dcproject.component.comment.constitution.routes.CreateConstitutionComment.CreateConstitutionCommentRequest.Input
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.generic.database.CommentForUpdate
import fr.dcproject.component.constitution.database.ConstitutionRef
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import org.joda.time.DateTime
import java.util.UUID

@KtorExperimentalLocationsAPI
object CreateConstitutionComment {
    @Location("/constitutions/{constitution}/comments")
    class CreateConstitutionCommentRequest(constitution: UUID) {
        val constitution = ConstitutionRef(constitution)
        class Input(val content: String)
    }

    fun Route.createConstitutionComment(repo: CommentConstitutionRepository, ac: CommentAccessControl) {
        post<CreateConstitutionCommentRequest> {
            call.receiveOrBadRequest<Input>().run {
                CommentForUpdate(
                    target = it.constitution,
                    createdBy = citizen,
                    content = content
                )
            }.let { comment ->
                ac.assert { canCreate(comment, citizenOrNull) }
                repo.comment(comment)

                call.respond(
                    HttpStatusCode.Created,
                    object {
                        val id: UUID = comment.id
                        val content: String = comment.content
                        val childrenCount: Int = 0
                        val createdAt: DateTime = comment.createdAt
                        val updatedAt: DateTime = comment.createdAt
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
}

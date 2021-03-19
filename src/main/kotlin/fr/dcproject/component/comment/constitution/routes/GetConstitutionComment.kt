package fr.dcproject.component.comment.constitution.routes

import fr.dcproject.common.dto.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.comment.constitution.database.CommentConstitutionRepository
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.constitution.database.ConstitutionRef
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
object GetConstitutionComment {
    @Location("/constitutions/{constitution}/comments")
    class GetConstitutionCommentRequest(constitution: UUID) {
        val constitution = ConstitutionRef(constitution)
    }

    fun Route.getConstitutionComment(repo: CommentConstitutionRepository, ac: CommentAccessControl) {
        get<GetConstitutionCommentRequest> {
            val comments = repo.findByTarget(it.constitution)
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
                }
            )
        }
    }
}

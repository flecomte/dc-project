package fr.dcproject.component.follow.routes.article

import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.follow.FollowAccessControl
import fr.dcproject.component.follow.database.FollowArticleRepository
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
object GetMyFollowsArticle {
    @Location("/citizens/{citizen}/follows/articles")
    class CitizenFollowArticleRequest(citizen: UUID) {
        val citizen = CitizenRef(citizen)
    }

    fun Route.getMyFollowsArticle(repo: FollowArticleRepository, ac: FollowAccessControl) {
        get<CitizenFollowArticleRequest> {
            mustBeAuth()
            val follows = repo.findByCitizen(it.citizen)
            ac.assert { canView(follows.result, citizenOrNull) }
            call.respond(
                HttpStatusCode.OK,
                follows.toOutput { f ->
                    object {
                        val id: UUID = f.id
                        val createdBy: Any = f.createdBy.toOutput()
                        val target: Any = f.target.let { t ->
                            object {
                                val id: UUID = t.id
                                val reference: String = f.target.reference
                            }
                        }
                        val createdAt: DateTime = f.createdAt
                    }
                }
            )
        }
    }
}

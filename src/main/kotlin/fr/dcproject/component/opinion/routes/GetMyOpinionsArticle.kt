package fr.dcproject.component.opinion.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.opinion.OpinionAccessControl
import fr.dcproject.component.opinion.database.Opinion
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import fr.postgresjson.connexion.Paginated
import io.konform.validation.Validation
import io.konform.validation.jsonschema.maximum
import io.konform.validation.jsonschema.minimum
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID
import fr.dcproject.component.opinion.database.OpinionRepositoryArticle as OpinionArticleRepository

@KtorExperimentalLocationsAPI
object GetMyOpinionsArticle {
    /**
     * Get paginated opinions of citizen for all articles
     */
    @Location("/citizens/{citizen}/opinions/articles")
    class CitizenOpinionsArticleRequest(
        citizen: UUID,
        page: Int = 1,
        limit: Int = 50
    ) : PaginatedRequestI by PaginatedRequest(page, limit) {
        val citizen = CitizenRef(citizen)
        fun validate() = Validation<CitizenOpinionsArticleRequest> {
            CitizenOpinionsArticleRequest::page {
                minimum(1)
            }
            CitizenOpinionsArticleRequest::limit {
                minimum(1)
                maximum(50)
            }
        }.validate(this)
    }

    fun Route.getMyOpinionsArticle(repo: OpinionArticleRepository, ac: OpinionAccessControl) {
        get<CitizenOpinionsArticleRequest> {
            mustBeAuth()
            it.validate().badRequestIfNotValid()

            val opinions: Paginated<Opinion<TargetRef>> = repo.findCitizenOpinions(citizen, it.page, it.limit)
            ac.canView(opinions.result, citizenOrNull).assert()
            call.respond(
                HttpStatusCode.OK,
                opinions.toOutput { it.toOutput() }
            )
        }
    }
}

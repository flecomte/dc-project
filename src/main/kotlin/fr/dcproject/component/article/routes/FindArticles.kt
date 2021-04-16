package fr.dcproject.component.article.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.common.validation.isUuid
import fr.dcproject.component.article.ArticleAccessControl
import fr.dcproject.component.article.database.ArticleForListing
import fr.dcproject.component.article.database.ArticleRepository
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.repository.RepositoryI
import io.konform.validation.Validation
import io.konform.validation.jsonschema.enum
import io.konform.validation.jsonschema.maximum
import io.konform.validation.jsonschema.minimum
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route

@KtorExperimentalLocationsAPI
object FindArticles {
    @Location("/articles")
    class ArticlesRequest(
        page: Int = 1,
        limit: Int = 50,
        val sort: String? = null,
        val direction: RepositoryI.Direction? = null,
        val search: String? = null,
        val createdBy: String? = null,
        val workgroup: String? = null
    ) : PaginatedRequestI by PaginatedRequest(page, limit) {
        fun validate() = Validation<ArticlesRequest> {
            ArticlesRequest::page {
                minimum(1)
            }
            ArticlesRequest::limit {
                minimum(1)
                maximum(50)
            }
            ArticlesRequest::sort ifPresent {
                enum(
                    "title",
                    "createdAt",
                    "vote",
                    "popularity",
                )
            }
            ArticlesRequest::createdBy ifPresent {
                isUuid()
            }
            ArticlesRequest::workgroup ifPresent {
                isUuid()
            }
        }.validate(this)
    }

    private fun ArticleRepository.findArticles(request: ArticlesRequest): Paginated<ArticleForListing> {
        return find(
            request.page,
            request.limit,
            request.sort,
            request.direction,
            request.search,
            ArticleRepository.Filter(createdById = request.createdBy, workgroupId = request.workgroup)
        )
    }

    fun Route.findArticles(repo: ArticleRepository, ac: ArticleAccessControl) {
        get<ArticlesRequest> {
            it.validate().badRequestIfNotValid()

            repo.findArticles(it)
                .apply { ac.assert { canView(result, citizenOrNull) } }
                .let {
                    call.respond(
                        it.toOutput {
                            object {
                                val id = it.id
                                val title = it.title
                                val createdAt = it.createdAt
                                val createdBy: Any = it.createdBy.toOutput()
                                val workgroup = it.workgroup?.let {
                                    object {
                                        val id = it.id
                                        val name = it.name
                                    }
                                }
                                val draft = it.draft
                            }
                        }
                    )
                }
        }
    }
}

package fr.dcproject.component.article.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.toUUID
import fr.dcproject.common.validation.isUuid
import fr.dcproject.component.article.ArticleAccessControl
import fr.dcproject.component.article.database.ArticleForListing
import fr.dcproject.component.article.database.ArticleRepository
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.routes.PaginatedRequest
import fr.dcproject.routes.PaginatedRequestI
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
object FindArticleVersions {
    @Location("/articles/{article}/versions")
    class ArticleVersionsRequest(
        val article: String,
        page: Int = 1,
        limit: Int = 50,
        val sort: String? = null,
        val direction: RepositoryI.Direction? = null,
        val search: String? = null
    ) : PaginatedRequestI by PaginatedRequest(page, limit) {
        fun validate() = Validation<ArticleVersionsRequest> {
            ArticleVersionsRequest::page {
                minimum(1)
                maximum(100)
            }
            ArticleVersionsRequest::limit {
                minimum(1)
                maximum(50)
            }
            ArticleVersionsRequest::sort ifPresent {
                enum(
                    "title",
                    "createdAt",
                    "vote",
                    "popularity",
                )
            }
            ArticleVersionsRequest::article {
                isUuid()
            }
        }.validate(this)
    }

    private fun ArticleRepository.findVersions(request: ArticleVersionsRequest) =
        findVersionsById(request.page, request.limit, request.article.toUUID())

    fun Route.findArticleVersions(repo: ArticleRepository, ac: ArticleAccessControl) {
        get<ArticleVersionsRequest> {
            it.validate().badRequestIfNotValid()

            repo.findVersions(it)
                .apply { ac.canView(result, citizenOrNull).assert() }
                .run {
                    call.respond(
                        toOutput { a: ArticleForListing ->
                            object {
                                val id = a.id
                                val title = a.title
                                val createdBy = object {
                                    val id = a.createdBy.id
                                    val name = a.createdBy.name.let { n ->
                                        object {
                                            val firstName = n.firstName
                                            val lastName = n.lastName
                                        }
                                    }
                                    val email = a.createdBy.email
                                }
                                val workgroup = a.workgroup?.let { w ->
                                    object {
                                        val id = w.id
                                        val name = w.name
                                    }
                                }
                                val draft = a.draft
                                val lastVersion = a.lastVersion
                            }
                        }
                    )
                }
        }
    }
}

package fr.dcproject.component.constitution.routes

import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.database.Citizen
import fr.dcproject.component.citizen.database.CitizenWithUserI
import fr.dcproject.component.constitution.ConstitutionAccessControl
import fr.dcproject.component.constitution.database.ConstitutionForUpdate
import fr.dcproject.component.constitution.database.ConstitutionForUpdate.TitleForUpdate
import fr.dcproject.component.constitution.database.ConstitutionRepository
import fr.dcproject.component.constitution.routes.CreateConstitution.PostConstitutionRequest.Input
import fr.dcproject.component.constitution.routes.CreateConstitution.PostConstitutionRequest.Input.Title
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
object CreateConstitution {
    @Location("/constitutions")
    class PostConstitutionRequest {
        class Input(
            val title: String,
            val anonymous: Boolean = true,
            val titles: MutableList<Title> = mutableListOf(),
            val draft: Boolean = false,
            val versionId: UUID = UUID.randomUUID()
        ) {

            class Title(
                val id: UUID = UUID.randomUUID(),
                val name: String,
                val articles: List<ArticleRef> = listOf()
            ) {
                class ArticleRef(val id: UUID)
            }
        }
    }

    private fun getNewConstitution(input: Input, citizen: Citizen) = input.run {
        ConstitutionForUpdate<CitizenWithUserI, TitleForUpdate<ArticleRef>>(
            id = UUID.randomUUID(),
            title = title,
            titles = titles.create(),
            createdBy = citizen,
            versionId = versionId
        )
    }

    private fun List<Title>.create(): MutableList<TitleForUpdate<ArticleRef>> =
        map { it.create() }.toMutableList()

    private fun Title.create(): TitleForUpdate<ArticleRef> =
        TitleForUpdate(
            id,
            name,
            articles.map { ArticleRef(it.id) }
        )

    fun Route.createConstitution(repo: ConstitutionRepository, ac: ConstitutionAccessControl) {
        post<PostConstitutionRequest> {
            getNewConstitution(call.receiveOrBadRequest(), citizen).let {
                ac.assert { canCreate(it, citizenOrNull) }
                val c = repo.upsert(it) ?: error("Unable to create Constitution")
                call.respond(
                    HttpStatusCode.Created,
                    object {
                        val id: UUID = c.id
                        val title: String = c.title
                        val titles: List<Any> = c.titles.map { t ->
                            object {
                                val id: UUID = t.id
                                val name: String = t.name
                                val rank: Int = t.rank
                                val articles: List<Any> = t.articles.map { a ->
                                    val id = a.id
                                    val title = a.title
                                    val createdBy = a.createdBy.toOutput()
                                    val workgroup: Any? = a.workgroup?.let { w ->
                                        object {
                                            val id = w.id
                                            val name = w.name
                                        }
                                    }
                                }
                            }
                        }
                        val anonymous: Boolean = c.anonymous
                        val draft: Boolean = c.draft
                        val versionId: UUID = c.versionId
                        val createdAt: DateTime = c.createdAt
                        val createdBy: Any = c.createdBy.toOutput()
                    }
                )
            }
        }
    }
}

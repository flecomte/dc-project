package fr.dcproject.routes

import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.CitizenWithUserI
import fr.dcproject.entity.ConstitutionSimple
import fr.dcproject.entity.ConstitutionSimple.TitleSimple
import fr.dcproject.security.assert
import fr.dcproject.security.voter.ConstitutionAccessControl
import fr.postgresjson.entity.UuidEntity
import fr.postgresjson.repository.RepositoryI
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID
import fr.dcproject.entity.Constitution as ConstitutionEntity
import fr.dcproject.repository.Constitution as ConstitutionRepository

@KtorExperimentalLocationsAPI
object ConstitutionPaths {
    @Location("/constitutions")
    class ConstitutionsRequest(
        page: Int = 1,
        limit: Int = 50,
        val sort: String? = null,
        val direction: RepositoryI.Direction? = null,
        val search: String? = null
    ) {
        val page: Int = if (page < 1) 1 else page
        val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
    }

    @Location("/constitutions/{constitution}")
    class ConstitutionRequest(val constitution: ConstitutionEntity)

    @Location("/constitutions")
    class PostConstitutionRequest {
        class Constitution(
            var title: String,
            var anonymous: Boolean = true,
            var titles: MutableList<Title> = mutableListOf(),
            var draft: Boolean = false,
            var lastVersion: Boolean = false,
            var versionId: UUID = UUID.randomUUID()
        ) {
            init {
                titles.forEachIndexed { index, title ->
                    title.rank = index
                }
            }

            class Title(
                id: UUID = UUID.randomUUID(),
                var name: String,
                var rank: Int? = null,
                var articles: MutableList<ArticleRef> = mutableListOf()
            ) : UuidEntity(id) {
                fun create(): TitleSimple<ArticleRef> =
                    TitleSimple(
                        id,
                        name,
                        rank,
                        articles
                    )
            }

            fun List<Title>.create(): MutableList<TitleSimple<ArticleRef>> =
                map { it.create() }.toMutableList()
        }

        suspend fun getNewConstitution(call: ApplicationCall): ConstitutionSimple<CitizenWithUserI, TitleSimple<ArticleRef>> = call.receive<Constitution>().run {
            ConstitutionSimple<CitizenWithUserI, TitleSimple<ArticleRef>>(
                id = UUID.randomUUID(),
                title = title,
                titles = titles.create(),
                createdBy = call.citizen,
                versionId = versionId
            )
        }
    }
}

@KtorExperimentalLocationsAPI
fun Route.constitution(repo: ConstitutionRepository, ac: ConstitutionAccessControl) {
    get<ConstitutionPaths.ConstitutionsRequest> {
        val constitutions = repo.find(it.page, it.limit, it.sort, it.direction, it.search)
        ac.assert { canView(constitutions.result, citizenOrNull) }
        call.respond(constitutions)
    }

    get<ConstitutionPaths.ConstitutionRequest> {
        ac.assert { canView(it.constitution, citizenOrNull) }
        call.respond(it.constitution)
    }

    post<ConstitutionPaths.PostConstitutionRequest> {
        it.getNewConstitution(call).let { constitution ->
            ac.assert { canCreate(constitution, citizenOrNull) }
            repo.upsert(constitution)
            call.respond(constitution)
        }
    }
}

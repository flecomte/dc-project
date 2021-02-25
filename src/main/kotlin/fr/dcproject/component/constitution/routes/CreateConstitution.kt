package fr.dcproject.component.constitution.routes

import fr.dcproject.common.entity.Entity
import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.citizen.Citizen
import fr.dcproject.component.citizen.CitizenWithUserI
import fr.dcproject.component.constitution.ConstitutionAccessControl
import fr.dcproject.component.constitution.ConstitutionRepository
import fr.dcproject.component.constitution.ConstitutionSimple
import fr.dcproject.component.constitution.ConstitutionSimple.TitleSimple
import fr.dcproject.component.constitution.routes.CreateConstitution.PostConstitutionRequest.Input
import fr.dcproject.component.constitution.routes.CreateConstitution.PostConstitutionRequest.Input.Title
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object CreateConstitution {
    @Location("/constitutions")
    class PostConstitutionRequest {
        class Input(
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
            ) : Entity(id)
        }
    }

    private fun getNewConstitution(input: Input, citizen: Citizen) = input.run {
        ConstitutionSimple<CitizenWithUserI, TitleSimple<ArticleRef>>(
            id = UUID.randomUUID(),
            title = title,
            titles = titles.create(),
            createdBy = citizen,
            versionId = versionId
        )
    }

    private fun List<Title>.create(): MutableList<TitleSimple<ArticleRef>> =
        map { it.create() }.toMutableList()

    private fun Title.create(): TitleSimple<ArticleRef> =
        TitleSimple(
            id,
            name,
            rank,
            articles
        )

    fun Route.createConstitution(repo: ConstitutionRepository, ac: ConstitutionAccessControl) {
        post<PostConstitutionRequest> {
            getNewConstitution(call.receiveOrBadRequest(), citizen).let {
                ac.assert { canCreate(it, citizenOrNull) }
                repo.upsert(it)
                call.respond(it)
            }
        }
    }
}

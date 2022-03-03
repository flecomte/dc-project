package fr.dcproject.component.constitution.routes

import fr.dcproject.common.response.toOutput
import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.constitution.ConstitutionAccessControl
import fr.dcproject.component.constitution.database.ConstitutionRef
import fr.dcproject.component.constitution.database.ConstitutionRepository
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
object GetConstitution {
    @Location("/constitutions/{constitution}")
    class GetConstitutionRequest(constitution: UUID) {
        val constitution = ConstitutionRef(constitution)
    }

    fun Route.getConstitution(ac: ConstitutionAccessControl, constitutionRepo: ConstitutionRepository) {
        get<GetConstitutionRequest> {
            val constitution = constitutionRepo.findById(it.constitution.id) ?: throw NotFoundException("Unable to find constitution ${it.constitution.id}")
            ac.canView(constitution, citizenOrNull).assert()
            call.respond(
                HttpStatusCode.OK,
                constitution.let { c ->
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
                }
            )
        }
    }
}

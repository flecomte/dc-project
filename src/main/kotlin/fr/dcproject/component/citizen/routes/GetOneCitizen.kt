package fr.dcproject.component.citizen.routes

import fr.dcproject.common.security.assert
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.citizen.CitizenAccessControl
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.citizen.database.CitizenRepository
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import java.util.UUID

@KtorExperimentalLocationsAPI
object GetOneCitizen {
    @Location("/citizens/{citizen}")
    class CitizenRequest(citizen: UUID) {
        val citizen = CitizenRef(citizen)
    }

    fun Route.getOneCitizen(ac: CitizenAccessControl, citizenRepository: CitizenRepository) {
        get<CitizenRequest> {
            mustBeAuth()
            val citizen = citizenRepository.findById(it.citizen.id) ?: throw NotFoundException("Citizen not found ${it.citizen.id}")
            ac.assert { canView(citizen, citizenOrNull) }

            call.respond(
                object {
                    val id: UUID = citizen.id
                    val name: Any = citizen.name.let { n ->
                        object {
                            val firstName: String = n.firstName
                            val lastName: String = n.lastName
                        }
                    }
                    val email: String = citizen.email
                    val birthday: String = citizen.birthday.run {
                        toString(ISODateTimeFormat.date())
                    }
                    val createdAt: DateTime = citizen.createdAt
                    val user: Any = object {
                        val username: String = citizen.user.username
                    }
                }
            )
        }
    }
}

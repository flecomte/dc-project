package fr.dcproject.component.workgroup.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.common.validation.isUrl
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.workgroup.WorkgroupAccessControl
import fr.dcproject.component.workgroup.database.WorkgroupForUpdate
import fr.dcproject.component.workgroup.database.WorkgroupRepository
import fr.dcproject.component.workgroup.routes.EditWorkgroup.PutWorkgroupRequest.Input
import io.konform.validation.Validation
import io.konform.validation.jsonschema.maxLength
import io.konform.validation.jsonschema.minLength
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.put
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object EditWorkgroup {
    @Location("/workgroups/{workgroupId}")
    class PutWorkgroupRequest(val workgroupId: UUID) {
        class Input(
            val name: String?,
            val description: String?,
            val logo: String?,
            val anonymous: Boolean?
        ) {
            fun validate() = Validation<Input> {
                Input::name ifPresent {
                    minLength(5)
                    maxLength(80)
                }
                Input::description ifPresent {
                    minLength(50)
                    maxLength(6000)
                }
                Input::logo ifPresent {
                    isUrl()
                    maxLength(2048)
                }
            }.validate(this)
        }
    }

    fun Route.editWorkgroup(repo: WorkgroupRepository, ac: WorkgroupAccessControl) {
        put<PutWorkgroupRequest> {
            mustBeAuth()
            repo.findById(it.workgroupId)?.let { old ->
                call.receiveOrBadRequest<Input>().run {
                    validate().badRequestIfNotValid()
                    WorkgroupForUpdate(
                        id = old.id,
                        name = name ?: old.name,
                        description = description ?: old.description,
                        createdBy = old.createdBy,
                        logo = logo ?: old.logo,
                        anonymous = anonymous ?: old.anonymous,
                        deletedAt = old.deletedAt,
                        members = old.members,
                    ).let { workgroup ->
                        ac.assert { canUpdate(workgroup, citizenOrNull) }
                        repo.upsert(workgroup)
                    }.let {
                        call.respond(HttpStatusCode.OK, it.toOutput())
                    }
                }
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }
}

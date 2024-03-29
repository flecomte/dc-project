package fr.dcproject.component.workgroup.routes

import fr.dcproject.application.http.badRequestIfNotValid
import fr.dcproject.common.security.assert
import fr.dcproject.common.utils.receiveOrBadRequest
import fr.dcproject.common.validation.isUrl
import fr.dcproject.component.auth.citizen
import fr.dcproject.component.auth.citizenOrNull
import fr.dcproject.component.auth.mustBeAuth
import fr.dcproject.component.workgroup.WorkgroupAccessControl
import fr.dcproject.component.workgroup.database.WorkgroupForUpdate
import fr.dcproject.component.workgroup.database.WorkgroupRepository
import fr.dcproject.component.workgroup.routes.CreateWorkgroup.PostWorkgroupRequest.Input
import io.konform.validation.Validation
import io.konform.validation.jsonschema.maxLength
import io.konform.validation.jsonschema.minLength
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.UUID

@KtorExperimentalLocationsAPI
object CreateWorkgroup {
    @Location("/workgroups")
    open class PostWorkgroupRequest {
        class Input(
            val id: UUID?,
            val name: String,
            val description: String,
            val logo: String?,
            val anonymous: Boolean?
        ) {
            fun validate() = Validation<Input> {
                Input::name {
                    minLength(5)
                    maxLength(80)
                }
                Input::description {
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

    fun Route.createWorkgroup(repo: WorkgroupRepository, ac: WorkgroupAccessControl) {
        post<PostWorkgroupRequest> {
            mustBeAuth()
            call.receiveOrBadRequest<Input>().run {
                validate().badRequestIfNotValid()

                WorkgroupForUpdate(
                    id ?: UUID.randomUUID(),
                    name,
                    description,
                    citizen,
                    logo,
                    anonymous ?: true,
                )
            }.let { workgroup ->
                ac.assert { canCreate(workgroup, citizenOrNull) }
                repo.upsert(workgroup)
            }.let { w ->
                call.respond(
                    HttpStatusCode.Created,
                    w.toOutput()
                )
            }
        }
    }
}

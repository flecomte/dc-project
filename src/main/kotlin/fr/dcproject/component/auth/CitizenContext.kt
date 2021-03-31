package fr.dcproject.component.auth

import fr.dcproject.component.auth.database.User
import fr.dcproject.component.auth.database.UserI
import fr.dcproject.component.citizen.database.CitizenRepository
import io.ktor.application.ApplicationCall
import io.ktor.auth.authentication
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import org.koin.core.context.GlobalContext
import fr.dcproject.component.citizen.database.Citizen as CitizenEntity

class ForbiddenException(message: String) : Exception(message)

private val citizenAttributeKey = AttributeKey<CitizenEntity>("CitizenContext")

val ApplicationCall.citizen: CitizenEntity
    get() = attributes.computeIfAbsent(citizenAttributeKey) {
        val user = authentication.principal<UserI>() ?: throw ForbiddenException("No User Connected")
        GlobalContext.get().koin.get<CitizenRepository>().findByUser(user)
            ?: throw ForbiddenException("Citizen not found for this user id \"${user.id}\"")
    }

val ApplicationCall.citizenOrNull: CitizenEntity?
    get() = authentication.principal<UserI>()?.let {
        GlobalContext.get().koin.get<CitizenRepository>().findByUser(it)
    }

val ApplicationCall.isAuth: Boolean
    get() = citizenOrNull == null

fun ApplicationCall.mustBeAuth() {
    citizenOrNull ?: throw ForbiddenException("No User Connected")
}

val PipelineContext<Unit, ApplicationCall>.citizen get() = context.citizen
val PipelineContext<Unit, ApplicationCall>.citizenOrNull get() = context.citizenOrNull

val ApplicationCall.user get() = authentication.principal<User>()

val PipelineContext<Unit, ApplicationCall>.isAuth: Boolean
    get() = citizenOrNull == null

fun PipelineContext<Unit, ApplicationCall>.mustBeAuth() {
    citizenOrNull ?: throw ForbiddenException("No User Connected")
}

package fr.dcproject.component.auth

import fr.dcproject.component.citizen.CitizenRepository
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import org.koin.core.context.GlobalContext
import fr.dcproject.component.citizen.Citizen as CitizenEntity

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

val PipelineContext<Unit, ApplicationCall>.citizen get() = context.citizen
val PipelineContext<Unit, ApplicationCall>.citizenOrNull get() = context.citizenOrNull

val ApplicationCall.user get() = authentication.principal<User>()
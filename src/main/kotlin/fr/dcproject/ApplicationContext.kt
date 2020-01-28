package fr.dcproject

import fr.dcproject.entity.UserI
import fr.dcproject.security.voter.ForbiddenException
import io.ktor.application.ApplicationCall
import io.ktor.auth.authentication
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext
import fr.dcproject.entity.Citizen as CitizenEntity
import fr.dcproject.repository.Citizen as CitizenRepository

private val citizenAttributeKey = AttributeKey<CitizenEntity>("CitizenContext")

val ApplicationCall.citizen: CitizenEntity
    get() = attributes.computeIfAbsent(citizenAttributeKey) {
        runBlocking {
            val user = authentication.principal<UserI>() ?: throw ForbiddenException()
            GlobalContext.get().koin.get<CitizenRepository>().findByUser(user) ?: throw ForbiddenException("Citizen not found for this user id \"${user.id}\"")
        }
    }

val PipelineContext<Unit, ApplicationCall>.citizen get() = context.citizen

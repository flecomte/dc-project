package fr.dcproject.security.voter

import fr.dcproject.entity.Citizen
import fr.dcproject.entity.User
import io.ktor.application.ApplicationCall
import io.ktor.locations.KtorExperimentalLocationsAPI

@KtorExperimentalLocationsAPI
class CitizenVoter : Voter {
    enum class Action : ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE,
        CHANGE_PASSWORD
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return (action is Action)
            .and(subject is List<*> || subject is Citizen?)
    }

    override fun vote(action: ActionI, call: ApplicationCall, subject: Any?): Vote {
        val user = call.user
        if (action == Action.CREATE && user != null) {
            return Vote.GRANTED
        }

        if (action == Action.VIEW) {
            if (user == null) return Vote.DENIED
            if (subject is Citizen) {
                return if (subject.isDeleted()) Vote.DENIED
                else Vote.GRANTED
            }
            if (subject is List<*>) {
                subject.forEach {
                    if (it !is Citizen || it.isDeleted()) {
                        return Vote.DENIED
                    }
                }
                return Vote.GRANTED
            }
            return Vote.DENIED
        }

        if (action == Action.DELETE) {
            return Vote.DENIED
        }

        if (action == Action.UPDATE &&
            user is User &&
            subject is Citizen &&
            subject.user?.id == user.id) {
            return Vote.GRANTED
        }

        if (action == Action.CHANGE_PASSWORD && user != null && subject is Citizen) {
            val userToChange = subject.user ?: error("Citizen must have User")
            return if (user.id == userToChange.id) {
                Vote.GRANTED
            } else {
                Vote.ABSTAIN
            }
        }

        if (action is Action) {
            return Vote.DENIED
        }

        return Vote.ABSTAIN
    }
}

package fr.dcproject.security.voter

import fr.dcproject.entity.Opinion
import io.ktor.application.ApplicationCall

class OpinionVoter : Voter {
    enum class Action : ActionI {
        CREATE,
        VIEW,
        DELETE
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return (action is Action)
            .and(subject is Opinion<*>? || subject is List<*>)
    }

    override fun vote(action: ActionI, call: ApplicationCall, subject: Any?): Vote {
        val user = call.user
        if (action == Action.CREATE) {
            return if (user != null) Vote.GRANTED else Vote.DENIED
        }

        if (action == Action.VIEW) {
            if (subject is Opinion<*>) {
                return Vote.GRANTED
            }
            if (subject is List<*>) {
                subject.forEach {
                    if (it !is Opinion<*>) {
                        return Vote.DENIED
                    }
                }
                return Vote.GRANTED
            }
            return Vote.DENIED
        }

        if (action == Action.DELETE) {
            return if (subject is Opinion<*>
                && user != null
                && subject.createdBy.user.id == user.id)
                Vote.GRANTED
            else Vote.DENIED
        }

        if (action is Action) {
            return Vote.DENIED
        }

        return Vote.ABSTAIN
    }
}

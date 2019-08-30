package fr.dcproject.security.voter

import io.ktor.application.ApplicationCall
import fr.dcproject.entity.Vote as VoteEntity

class VoteVoter: Voter {
    enum class Action: ActionI {
        CREATE,
        VIEW
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return action is Action && subject is VoteEntity<*>?
    }

    override fun vote(action: ActionI, call: ApplicationCall, subject: Any?): Vote {
        val user = call.user
        if (action == Action.CREATE && user != null) {
            return Vote.GRANTED
        }

        if (action == Action.VIEW) {
            return Vote.GRANTED
        }

        return Vote.ABSTAIN
    }
}

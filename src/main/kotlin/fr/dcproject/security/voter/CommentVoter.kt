package fr.dcproject.security.voter

import fr.dcproject.entity.Comment
import io.ktor.application.ApplicationCall

class CommentVoter: Voter {
    enum class Action: ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return action is Action && subject is Comment<*>?
    }

    override fun vote(action: ActionI, call: ApplicationCall, subject: Any?): Vote {
        val user = call.user
        if (action == Action.CREATE && user != null) {
            return Vote.GRANTED
        }

        if (action == Action.VIEW) {
            return Vote.GRANTED
        }

        if (action == Action.UPDATE && user != null && subject is Comment<*> && user.id == subject.createdBy?.userId) {
            return Vote.GRANTED
        }

        if (action == Action.DELETE) {
            return Vote.DENIED
        }

        return Vote.ABSTAIN
    }
}

package fr.dcproject.security.voter

import fr.dcproject.entity.Constitution
import fr.dcproject.entity.User
import io.ktor.application.ApplicationCall

class ConstitutionVoter: Voter {
    enum class Action: ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return (action is Action || action is CommentVoter.Action) && subject is Constitution?
    }

    override fun vote(action: ActionI, call: ApplicationCall, subject: Any?): Vote {
        val user = call.user
        if (action == Action.CREATE && user != null) {
            return Vote.GRANTED
        }

        if (action == Action.VIEW) {
            return Vote.GRANTED
        }

        if (action == CommentVoter.Action.CREATE) {
            return Vote.GRANTED
        }

        if (action == CommentVoter.Action.VIEW) {
            return Vote.GRANTED
        }

        if (action == Action.DELETE && user is User && subject is Constitution && subject.createdBy?.userId == user.id) {
            return Vote.GRANTED
        }

        if (action == Action.UPDATE && user is User && subject is Constitution && subject.createdBy?.userId == user.id) {
            return Vote.GRANTED
        }

        return Vote.ABSTAIN
    }
}

package fr.dcproject.security.voter

import fr.dcproject.entity.Article
import fr.dcproject.entity.User
import io.ktor.application.ApplicationCall

class ArticleVoter: Voter {
    enum class Action: ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return action is Action && subject is Article?
    }

    override fun vote(action: ActionI, call: ApplicationCall, subject: Any?): Vote {
        val user = call.user
        if (action == Action.CREATE && user != null) {
            return Vote.GRANTED
        }

        if (action == Action.VIEW) {
            return Vote.GRANTED
        }

        if (action == Action.DELETE && user is User && subject is Article && subject.createdBy?.userId == user.id) {
            return Vote.GRANTED
        }

        if (action == Action.UPDATE && user is User && subject is Article && subject.createdBy?.userId == user.id) {
            return Vote.GRANTED
        }

        return Vote.ABSTAIN
    }
}

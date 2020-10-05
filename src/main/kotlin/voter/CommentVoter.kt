package fr.dcproject.security.voter

import fr.dcproject.entity.Comment
import fr.dcproject.user
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.Voter
import io.ktor.application.ApplicationCall

class CommentVoter : Voter<ApplicationCall> {
    enum class Action : ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE
    }

    override fun invoke(action: Any, context: ApplicationCall, subject: Any?): Vote {
        if (!(action is Action && subject is Comment<*>?)) return Vote.ABSTAIN

        val user = context.user

        if (subject == null) {
            return Vote.DENIED
        }

        if (action == Action.CREATE) {
            if (user == null) {
                return Vote.DENIED
            }
            if (subject.createdBy.user.id != user.id) {
                return Vote.DENIED
            }
            return Vote.GRANTED
        }

        if (action == Action.VIEW) {
            return if (subject.isDeleted()) Vote.DENIED
            else Vote.GRANTED
        }

        if (action == Action.UPDATE && user != null && user.id == subject.createdBy.user.id) {
            return Vote.GRANTED
        }

        if (action == Action.DELETE) {
            return Vote.DENIED
        }

        return Vote.DENIED
    }
}

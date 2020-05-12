package fr.dcproject.security.voter

import fr.dcproject.entity.Comment
import fr.dcproject.user
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.Voter
import io.ktor.application.ApplicationCall

class CommentVoter : Voter {
    enum class Action : ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return (action is Action)
            .and(subject is Comment<*>?)
    }

    override fun vote(action: ActionI, call: ApplicationCall, subject: Any?): Vote {
        val user = call.user

        if (subject !is Comment<*>) {
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

        if (action is Action) {
            return Vote.DENIED
        }

        return Vote.ABSTAIN
    }
}

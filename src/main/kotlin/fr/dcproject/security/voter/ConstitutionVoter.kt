package fr.dcproject.security.voter

import fr.dcproject.entity.User
import io.ktor.application.ApplicationCall
import fr.dcproject.entity.Constitution as ConstitutionEntity
import fr.dcproject.entity.Vote as VoteEntity

class ConstitutionVoter: Voter {
    enum class Action: ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return (action is Action || action is CommentVoter.Action) && subject is ConstitutionEntity?
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

        if (action == Action.DELETE && user is User && subject is ConstitutionEntity && subject.createdBy?.userId == user.id) {
            return Vote.GRANTED
        }

        if (action == Action.UPDATE && user is User && subject is ConstitutionEntity && subject.createdBy?.userId == user.id) {
            return Vote.GRANTED
        }

        if (action == VoteVoter.Action.CREATE && subject is VoteEntity<*>) {
            val target = subject.target
            if (target !is ConstitutionEntity) {
                return Vote.ABSTAIN
            }
            if (target.isDeleted()) {
                return Vote.DENIED
            }
        }

        if (action is Action) {
            return Vote.DENIED
        }

        return Vote.ABSTAIN
    }
}

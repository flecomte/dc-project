package fr.dcproject.security.voter

import fr.dcproject.user
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.Voter
import io.ktor.application.ApplicationCall
import fr.dcproject.entity.Vote as VoteEntity

class VoteVoter : Voter {
    enum class Action : ActionI {
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

        if (action == Action.VIEW && user != null) {
            if (subject is VoteEntity<*>) {
                return if (subject.createdBy.user.id != user.id) {
                    Vote.DENIED
                } else {
                    Vote.GRANTED
                }
            }
            return Vote.DENIED
        }

        return Vote.ABSTAIN
    }
}

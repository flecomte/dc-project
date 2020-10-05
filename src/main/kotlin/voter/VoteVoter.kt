package fr.dcproject.security.voter

import fr.dcproject.user
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.Voter
import io.ktor.application.ApplicationCall
import fr.dcproject.entity.Vote as VoteEntity

class VoteVoter : Voter<ApplicationCall> {
    enum class Action : ActionI {
        CREATE,
        VIEW
    }

    override fun invoke(action: Any, context: ApplicationCall, subject: Any?): Vote {
        if (!(action is Action && subject is VoteEntity<*>?)) return Vote.ABSTAIN

        val user = context.user ?: return Vote.DENIED

        if (action == Action.CREATE) {
            return Vote.GRANTED
        }

        if (action == Action.VIEW) {
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

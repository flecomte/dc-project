package fr.dcproject.security.voter

import fr.dcproject.entity.OpinionChoice
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.Voter
import io.ktor.application.ApplicationCall

class OpinionChoiceVoter : Voter {
    enum class Action : ActionI {
        VIEW
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return (action is Action)
            .and(subject is OpinionChoice?)
    }

    override fun vote(action: ActionI, call: ApplicationCall, subject: Any?): Vote {
        if (action == Action.VIEW) {
            if (subject is OpinionChoice) {
                return Vote.GRANTED
            }
            return Vote.DENIED
        }

        return Vote.ABSTAIN
    }
}

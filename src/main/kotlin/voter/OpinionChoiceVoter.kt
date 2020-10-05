package fr.dcproject.security.voter

import fr.dcproject.entity.OpinionChoice
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.Voter
import io.ktor.application.ApplicationCall

class OpinionChoiceVoter : Voter<ApplicationCall> {
    enum class Action : ActionI {
        VIEW
    }

    override fun invoke(action: Any, context: ApplicationCall, subject: Any?): Vote {
        if (!((action is Action)
            && (subject is OpinionChoice?))) return Vote.ABSTAIN

        if (action == Action.VIEW) {
            if (subject is OpinionChoice) {
                return Vote.GRANTED
            }
            return Vote.DENIED
        }

        return Vote.ABSTAIN
    }
}

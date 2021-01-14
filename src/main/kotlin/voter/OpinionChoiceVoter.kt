package fr.dcproject.security.voter

import fr.dcproject.entity.OpinionChoice
import fr.dcproject.voter.NoSubjectDefinedException
import fr.ktorVoter.*
import io.ktor.application.*

class OpinionChoiceVoter : Voter<ApplicationCall> {
    enum class Action : ActionI {
        VIEW
    }

    override fun invoke(action: Any, context: ApplicationCall, subject: Any?): VoterResponseI {
        if (!((action is Action) &&
            (subject is OpinionChoice?))) return abstain()

        if (action == Action.VIEW) {
            if (subject is OpinionChoice) {
                return granted()
            }
            throw NoSubjectDefinedException(action)
        }

        return abstain()
    }
}

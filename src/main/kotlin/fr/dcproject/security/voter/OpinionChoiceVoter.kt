package fr.dcproject.security.voter

import fr.dcproject.entity.OpinionChoice
import io.ktor.application.ApplicationCall

class OpinionChoiceVoter : Voter {
    enum class Action : ActionI {
        VIEW
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return (action is Action)
            .and(subject is OpinionChoice? || subject is List<*>)
    }

    override fun vote(action: ActionI, call: ApplicationCall, subject: Any?): Vote {
        if (action == Action.VIEW) {
            if (subject is OpinionChoice) {
                return Vote.GRANTED
            }
            if (subject is List<*>) {
                subject.forEach {
                    if (it !is OpinionChoice) {
                        return Vote.DENIED
                    }
                }
                return Vote.GRANTED
            }
            return Vote.DENIED
        }

        return Vote.ABSTAIN
    }
}

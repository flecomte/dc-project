package fr.dcproject.security.voter

import fr.dcproject.entity.UserI
import fr.dcproject.entity.WorkgroupI
import fr.dcproject.entity.WorkgroupWithAuthI
import io.ktor.application.ApplicationCall

class WorkgroupVoter : Voter {
    enum class Action : ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return (action is Action)
            .and(subject is WorkgroupI?)
    }

    override fun vote(action: ActionI, call: ApplicationCall, subject: Any?): Vote {
        val user = call.user
        if (subject is WorkgroupI && action == Action.CREATE && user is UserI) {
            return Vote.GRANTED
        }

        if (action == Action.VIEW) {
            if (subject is WorkgroupWithAuthI<*>) {
                return if (subject.isDeleted()) Vote.DENIED
                else if (!subject.anonymous) Vote.GRANTED
                else if (subject.anonymous && user != null && subject.isMember(user)) Vote.GRANTED
                else Vote.DENIED
            }
            return Vote.DENIED
        }

        if (subject is WorkgroupWithAuthI<*>) {
            if (action == Action.DELETE && user is UserI && subject.owner.user.id == user.id) {
                return Vote.GRANTED
            }

            if (action == Action.UPDATE && user is UserI && subject.owner.user.id == user.id) {
                return Vote.GRANTED
            }

            return Vote.DENIED
        } else if (subject !is WorkgroupWithAuthI<*> && (action == Action.DELETE || action == Action.UPDATE)) {
            throw object :
                VoterException("Unable to define if your are granted, the subject must implement 'WorkgroupWithAuthI'") {}
        }

        return Vote.ABSTAIN
    }
}

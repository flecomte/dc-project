package fr.dcproject.security.voter

import fr.dcproject.entity.*
import fr.dcproject.entity.WorkgroupWithMembersI.Member.Role
import fr.dcproject.user
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.Voter
import fr.ktorVoter.VoterException
import io.ktor.application.ApplicationCall

class WorkgroupVoter : Voter {
    enum class Action : ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE,
    }

    enum class ActionMembers : ActionI {
        ADD,
        UPDATE,
        VIEW,
        REMOVE,
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return (action is Action || action is ActionMembers)
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
            if (action == Action.DELETE && user is UserI && subject.hasRole(Role.MASTER, user)) {
                return Vote.GRANTED
            }

            if (action == Action.UPDATE && user is UserI && subject.hasRole(Role.MASTER, user)) {
                return Vote.GRANTED
            }

            return Vote.DENIED
        } else if (subject !is WorkgroupWithAuthI<*> && (action == Action.DELETE || action == Action.UPDATE)) {
            throw object :
                VoterException("Unable to define if your are granted, the subject must implement 'WorkgroupWithAuthI'") {}
        }

        if (action == ActionMembers.ADD) {
            // TODO create ROLES
            return Vote.isGranted {
                user is UserI &&
                subject is WorkgroupWithAuthI<*> &&
                subject.hasRole(Role.MASTER, user)
            }
        }

        if (action == ActionMembers.UPDATE) {
            // TODO create ROLES
            return Vote.isGranted {
                user is UserI &&
                subject is WorkgroupWithAuthI<*> &&
                subject.hasRole(Role.MASTER, user)
            }
        }

        if (action == ActionMembers.REMOVE) {
            // TODO create ROLES
            return Vote.isGranted {
                user is UserI &&
                subject is WorkgroupWithAuthI<*> &&
                subject.hasRole(Role.MASTER, user)
            }
        }

        return Vote.ABSTAIN
    }
}

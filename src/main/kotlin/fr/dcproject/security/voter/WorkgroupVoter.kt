package fr.dcproject.security.voter

import fr.dcproject.citizenOrNull
import fr.dcproject.entity.*
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

        if (action == ActionMembers.ADD) {
            val citizen = call.citizenOrNull
            // TODO create ROLES
            return Vote.isGranted {
                citizen != null &&
                subject is WorkgroupWithMembersI<*> &&
                subject.members.asCitizen(citizen)
            }
        }

        if (action == ActionMembers.UPDATE) {
            val citizen = call.citizenOrNull
            // TODO create ROLES
            return Vote.isGranted {
                citizen != null &&
                subject is WorkgroupWithMembersI<*> &&
                subject.members.asCitizen(citizen)
            }
        }

        if (action == ActionMembers.REMOVE) {
            val citizen = call.citizenOrNull
            // TODO create ROLES
            return Vote.isGranted {
                citizen != null &&
                subject is WorkgroupWithMembersI<*> &&
                subject.members.asCitizen(citizen)
            }
        }

        return Vote.ABSTAIN
    }
}

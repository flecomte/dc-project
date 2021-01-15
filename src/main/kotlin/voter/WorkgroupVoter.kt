package fr.dcproject.security.voter

import fr.dcproject.component.auth.UserI
import fr.dcproject.entity.WorkgroupI
import fr.dcproject.entity.WorkgroupWithAuthI
import fr.dcproject.entity.WorkgroupWithMembersI.Member.Role
import fr.dcproject.user
import fr.dcproject.voter.NoRuleDefinedException
import fr.dcproject.voter.NoSubjectDefinedException
import fr.ktorVoter.*
import io.ktor.application.*

class WorkgroupVoter : Voter<ApplicationCall> {
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

    override fun invoke(action: Any, context: ApplicationCall, subject: Any?): VoterResponseI {
        if ((action is Action && subject == null)) throw NoSubjectDefinedException(action)
        if (!((action is Action || action is ActionMembers) &&
            (subject is WorkgroupI? || (subject is List<*> && subject.first() is WorkgroupI)))) return abstain()

        val user = context.user
        if (action == Action.CREATE) {
            if (user == null) return denied("You must be connected to delete workgroup", "workgroup.delete.notConnected")
            if (subject is WorkgroupI) {
                return granted()
            }
        }

        if (action == Action.VIEW) {
            if (subject is WorkgroupWithAuthI<*>) {
                return if (subject.isDeleted()) denied("You cannot view a deleted workgroup", "workgroup.view.deleted")
                else if (!subject.anonymous) granted()
                else if (subject.anonymous && user != null && subject.isMember(user)) granted()
                else denied("You cannot view anonymous workgroup", "workgroup.view.anonymous")
            }
            throw NoSubjectDefinedException(action as ActionI)
        }

        if (subject is WorkgroupWithAuthI<*> && (action == Action.DELETE || action == Action.UPDATE)) {
            if (action == Action.DELETE) {
                if (user == null) return denied("You must be connected to delete workgroup", "workgroup.delete.notConnected")
                return if (subject.hasRole(Role.MASTER, user)) granted()
                else denied("You must hase role MASTER to delete workgroup", "workgroup.delete.role")
            }
            if (action == Action.UPDATE) {
                if (user == null) return denied("You must be connected to delete workgroup", "workgroup.delete.notConnected")
                return if (subject.hasRole(Role.MASTER, user)) granted()
                else denied("You must hase role MASTER to delete workgroup", "workgroup.delete.role")
            }

            throw NoRuleDefinedException(action as ActionI)
        } else if (subject !is WorkgroupWithAuthI<*> && (action == Action.DELETE || action == Action.UPDATE)) {
            throw NoSubjectDefinedException(action as ActionI)
        }

        if (action == ActionMembers.ADD) {
            // TODO create ROLES
            if (user !is UserI) return denied("You must be connected to add member to the workgroup", "workgroup.addMember.notConnected")
            if (subject !is WorkgroupWithAuthI<*>) throw NoSubjectDefinedException(action as ActionI)
            return if (subject.hasRole(Role.MASTER, user)) granted() else denied("You must have MASTER Role for add member to workgroup", "workgroup.addMember.role")
        }

        if (action == ActionMembers.UPDATE) {
            // TODO create ROLES
            if (user !is UserI) return denied("You must be connected to update member of the workgroup", "workgroup.updateMember.notConnected")
            if (subject !is WorkgroupWithAuthI<*>) throw NoSubjectDefinedException(action as ActionI)
            return if (subject.hasRole(Role.MASTER, user)) granted() else denied("You must have MASTER Role for update members of workgroup", "workgroup.updateMember.role")
        }

        if (action == ActionMembers.REMOVE) {
            // TODO create ROLES
            if (user !is UserI) return denied("You must be connected to remove member of the workgroup", "workgroup.removeMember.notConnected")
            if (subject !is WorkgroupWithAuthI<*>) throw NoSubjectDefinedException(action as ActionI)
            return if (subject.hasRole(Role.MASTER, user)) granted() else denied("You must have MASTER Role for remove members of workgroup", "workgroup.removeMember.role")
        }

        if (action is Action) {
            throw NoRuleDefinedException(action)
        }

        return abstain()
    }
}

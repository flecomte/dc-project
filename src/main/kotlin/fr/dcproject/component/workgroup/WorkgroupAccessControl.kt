package fr.dcproject.component.workgroup

import fr.dcproject.common.security.AccessControl
import fr.dcproject.common.security.AccessResponse
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.workgroup.database.WorkgroupI
import fr.dcproject.component.workgroup.database.WorkgroupWithAuthI
import fr.dcproject.component.workgroup.database.WorkgroupWithMembersI.Member.Role

class WorkgroupAccessControl : AccessControl() {
    fun canCreate(subject: WorkgroupI, citizen: CitizenI?): AccessResponse {
        if (citizen == null) return denied("You must be connected to create workgroup", "workgroup.create.notConnected")
        return granted()
    }

    fun <S : WorkgroupWithAuthI<*>> canView(subjects: List<S>, citizen: CitizenI?): AccessResponse =
        canAll(subjects) { canView(it, citizen) }

    fun canView(subject: WorkgroupWithAuthI<*>, citizen: CitizenI?): AccessResponse =
        if (subject.isDeleted()) denied("You cannot view a deleted workgroup", "workgroup.view.deleted")
        else if (!subject.anonymous) granted()
        else if (subject.anonymous && citizen != null && subject.isMember(citizen)) granted()
        else denied("You cannot view anonymous workgroup", "workgroup.view.anonymous")

    fun canDelete(subject: WorkgroupWithAuthI<*>, citizen: CitizenI?): AccessResponse {
        if (citizen == null) return denied("You must be connected to delete workgroup", "workgroup.delete.notConnected")
        return if (subject.hasRole(Role.MASTER, citizen)) granted()
        else denied("You must hase role MASTER to delete workgroup", "workgroup.delete.role")
    }
    fun canUpdate(subject: WorkgroupWithAuthI<*>, citizen: CitizenI?): AccessResponse {
        if (citizen == null) return denied("You must be connected to update workgroup", "workgroup.update.notConnected")
        return if (subject.hasRole(Role.MASTER, citizen)) granted()
        else denied("You must hase role MASTER to delete workgroup", "workgroup.delete.role")
    }

    fun canAddMembers(subject: WorkgroupWithAuthI<*>, citizen: CitizenI?): AccessResponse = when {
        citizen == null -> denied("You must be connected to add member to the workgroup", "workgroup.addMember.notConnected")
        subject.hasRole(Role.MASTER, citizen) -> granted()
        else -> denied("You must have MASTER Role for add member to workgroup", "workgroup.addMember.role")
    }

    fun canUpdateMembers(subject: WorkgroupWithAuthI<*>, citizen: CitizenI?): AccessResponse = when {
        citizen == null -> denied("You must be connected to update member of the workgroup", "workgroup.updateMember.notConnected")
        subject.hasRole(Role.MASTER, citizen) -> granted()
        else -> denied("You must have MASTER Role for update members of workgroup", "workgroup.updateMember.role")
    }

    fun canRemoveMembers(subject: WorkgroupWithAuthI<*>, citizen: CitizenI?): AccessResponse = when {
        citizen == null -> denied("You must be connected to remove member of the workgroup", "workgroup.removeMember.notConnected")
        subject.hasRole(Role.MASTER, citizen) -> granted()
        else -> denied("You must have MASTER Role for remove members of workgroup", "workgroup.removeMember.role")
    }
}

package fr.dcproject.component.follow

import fr.dcproject.common.security.AccessControl
import fr.dcproject.common.security.AccessResponse
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.follow.database.FollowForView
import fr.dcproject.component.follow.database.FollowI

class FollowAccessControl : AccessControl() {
    fun canCreate(subject: FollowI, citizen: CitizenI?): AccessResponse {
        return if (citizen == null) denied("You must be connected to follow", "follow.create.notConnected")
        else granted()
    }

    fun canDelete(subject: FollowI, citizen: CitizenI?): AccessResponse {
        return if (citizen == null) denied("You must be connected to unfollow", "follow.delete.notConnected")
        else granted()
    }

    fun <S : FollowForView<*>> canView(subjects: List<S>, citizen: CitizenI?): AccessResponse =
        canAll(subjects) { canView(it, citizen) }

    fun canView(subject: FollowForView<*>, citizen: CitizenI?): AccessResponse {
        return if ((citizen != null && subject.createdBy.id == citizen.id) || !subject.createdBy.followAnonymous) granted()
        else denied("You cannot view an anonymous follow", "follow.view.anonymous")
    }
}

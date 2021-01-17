package fr.dcproject.security.voter

import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.entity.FollowI
import fr.dcproject.voter.Voter
import fr.dcproject.voter.VoterResponse
import fr.dcproject.entity.Follow as FollowEntity

class FollowVoter : Voter() {
    fun canCreate(subject: FollowI, citizen: CitizenI?): VoterResponse {
        return if (citizen == null) denied("You must be connected to follow", "follow.create.notConnected")
        else granted()
    }

    fun canDelete(subject: FollowI, citizen: CitizenI?): VoterResponse {
        return if (citizen == null) denied("You must be connected to unfollow", "follow.delete.notConnected")
        else granted()
    }

    fun <S : FollowEntity<*>> canView(subjects: List<S>, citizen: CitizenI?): VoterResponse =
        canAll(subjects) { canView(it, citizen) }

    fun canView(subject: FollowEntity<*>, citizen: CitizenI?): VoterResponse {
        return if ((citizen != null && subject.createdBy.id == citizen.id) || !subject.createdBy.followAnonymous) granted()
        else denied("You cannot view an anonymous follow", "follow.view.anonymous")
    }
}

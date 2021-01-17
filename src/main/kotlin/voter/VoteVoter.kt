package fr.dcproject.security.voter

import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.entity.TargetI
import fr.dcproject.entity.VoteForUpdateI
import fr.dcproject.voter.Voter
import fr.dcproject.voter.VoterResponse
import fr.postgresjson.entity.EntityDeletedAt
import fr.dcproject.entity.Vote as VoteEntity

class VoteVoter : Voter() {
    fun <S> canCreate(subject: VoteForUpdateI<S, *>, citizen: CitizenI?): VoterResponse where S : EntityDeletedAt, S : TargetI = when {
        citizen == null -> denied("You must be connected for vote", "vote.create.connected")
        subject.target.isDeleted() -> denied("You cannot vote on deleted target", "vote.create.isDeleted")
        else -> granted()
    }

    fun <S : VoteEntity<*>> canView(subjects: List<S>, citizen: CitizenI?): VoterResponse =
        canAll(subjects) { canView(it, citizen) }

    fun canView(subject: VoteEntity<*>, citizen: CitizenI?): VoterResponse = when {
        citizen == null -> denied("You must be connected for view your votes", "vote.view.connected")
        subject.createdBy.id != citizen.id -> denied("You can only display your votes", "vote.view.onlyYours")
        else -> granted()
    }
}

package fr.dcproject.component.vote

import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.security.AccessControl
import fr.dcproject.common.security.AccessResponse
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.vote.entity.VoteForUpdateI
import fr.postgresjson.entity.EntityDeletedAt
import fr.dcproject.component.vote.entity.Vote as VoteEntity

class VoteAccessControl : AccessControl() {
    fun <S> canCreate(subject: VoteForUpdateI<S, *>, citizen: CitizenI?): AccessResponse where S : EntityDeletedAt, S : TargetI = when {
        citizen == null -> denied("You must be connected for vote", "vote.create.connected")
        subject.target.isDeleted() -> denied("You cannot vote on deleted target", "vote.create.isDeleted")
        else -> granted()
    }

    fun <S : VoteEntity<*>> canView(subjects: List<S>, citizen: CitizenI?): AccessResponse =
        canAll(subjects) { canView(it, citizen) }

    fun canView(subject: VoteEntity<*>, citizen: CitizenI?): AccessResponse = when {
        citizen == null -> denied("You must be connected for view your votes", "vote.view.connected")
        subject.createdBy.id != citizen.id -> denied("You can only display your votes", "vote.view.onlyYours")
        else -> granted()
    }
}

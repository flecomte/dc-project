package fr.dcproject.component.opinion

import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.opinion.entity.OpinionI
import fr.dcproject.entity.HasTarget
import fr.dcproject.voter.Voter
import fr.dcproject.voter.VoterResponse
import fr.postgresjson.entity.EntityCreatedBy
import fr.postgresjson.entity.EntityDeletedAt

class OpinionVoter : Voter() {

    fun <S> canCreate(subjects: List<S>, citizen: CitizenI?): VoterResponse where S : OpinionI, S : HasTarget<*> =
        canAll(subjects) { canCreate(it, citizen) }

    fun <S> canCreate(subject: S, citizen: CitizenI?): VoterResponse where S : OpinionI, S : HasTarget<*> {
        val target = subject.target
        return when {
            citizen == null -> denied("You must be connected to make an opinion", "opinion.create.notConnected")
            target is EntityDeletedAt && target.isDeleted() -> denied("You cannot make opinion on deleted target", "opinion.create.deletedTarget")
            else -> granted()
        }
    }

    fun <S : OpinionI, SS : List<S>> canView(subjects: SS, citizen: CitizenI?): VoterResponse =
        canAll(subjects) { canView(it, citizen) }

    fun <S : OpinionI> canView(subject: S, citizen: CitizenI?): VoterResponse = granted()

    fun <S, C : CitizenI> canDelete(subject: S, citizen: CitizenI?): VoterResponse where S : EntityCreatedBy<C>, S : OpinionI = when {
        citizen == null -> denied("You must be connected to delete opinion", "opinion.delete.notConnected")
        subject.createdBy.id != citizen.id -> denied("You can only delete your opinions", "opinion.delete.notYours")
        else -> granted()
    }
}

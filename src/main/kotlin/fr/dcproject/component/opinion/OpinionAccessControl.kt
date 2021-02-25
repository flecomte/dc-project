package fr.dcproject.component.opinion

import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.DeletedAt
import fr.dcproject.common.entity.HasTarget
import fr.dcproject.common.security.AccessControl
import fr.dcproject.common.security.AccessResponse
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.opinion.entity.OpinionI

class OpinionAccessControl : AccessControl() {

    fun <S> canCreate(subjects: List<S>, citizen: CitizenI?): AccessResponse where S : OpinionI, S : HasTarget<*> =
        canAll(subjects) { canCreate(it, citizen) }

    fun <S> canCreate(subject: S, citizen: CitizenI?): AccessResponse where S : OpinionI, S : HasTarget<*> {
        val target = subject.target
        return when {
            citizen == null -> denied("You must be connected to make an opinion", "opinion.create.notConnected")
            target is DeletedAt && target.isDeleted() -> denied("You cannot make opinion on deleted target", "opinion.create.deletedTarget")
            else -> granted()
        }
    }

    fun <S, SS : List<S>, C : CitizenI> canView(subjects: SS, citizen: CitizenI?): AccessResponse where S : OpinionI, S : CreatedBy<C> =
        canAll(subjects) { canView(it, citizen) }

    fun <S, C : CitizenI> canView(subject: S, citizen: CitizenI?): AccessResponse where S : OpinionI, S : CreatedBy<C> = when {
        citizen == null -> denied("You must be connected to delete opinion", "opinion.delete.notConnected")
        subject.createdBy.id != citizen.id -> denied("You cannot view opinions of other citizen", "opinion.view.otherCitizen")
        else -> granted()
    }

    fun <S, C : CitizenI> canDelete(subject: S, citizen: CitizenI?): AccessResponse where S : CreatedBy<C>, S : OpinionI = when {
        citizen == null -> denied("You must be connected to delete opinion", "opinion.delete.notConnected")
        subject.createdBy.id != citizen.id -> denied("You can only delete your opinions", "opinion.delete.notYours")
        else -> granted()
    }
}

package fr.dcproject.component.opinion

import fr.dcproject.common.entity.HasTarget
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.opinion.entity.Opinion
import fr.dcproject.component.opinion.entity.OpinionI
import fr.dcproject.security.AccessControl
import fr.dcproject.security.AccessResponse
import fr.postgresjson.entity.EntityCreatedBy
import fr.postgresjson.entity.EntityDeletedAt

class OpinionAccessControl : AccessControl() {

    fun <S> canCreate(subjects: List<S>, citizen: CitizenI?): AccessResponse where S : OpinionI, S : HasTarget<*> =
        canAll(subjects) { canCreate(it, citizen) }

    fun <S> canCreate(subject: S, citizen: CitizenI?): AccessResponse where S : OpinionI, S : HasTarget<*> {
        val target = subject.target
        return when {
            citizen == null -> denied("You must be connected to make an opinion", "opinion.create.notConnected")
            target is EntityDeletedAt && target.isDeleted() -> denied("You cannot make opinion on deleted target", "opinion.create.deletedTarget")
            else -> granted()
        }
    }

    fun <S, SS : List<S>, C: CitizenI> canView(subjects: SS, citizen: CitizenI?): AccessResponse where S : OpinionI, S : EntityCreatedBy<C> =
        canAll(subjects) { canView(it, citizen) }

    fun <S, C: CitizenI> canView(subject: S, citizen: CitizenI?): AccessResponse where S : OpinionI, S : EntityCreatedBy<C> = when {
        citizen == null -> denied("You must be connected to delete opinion", "opinion.delete.notConnected")
        subject.createdBy.id != citizen.id -> denied("You cannot view opinions of other citizen", "opinion.view.otherCitizen")
        else -> granted()
    }

    fun <S, C : CitizenI> canDelete(subject: S, citizen: CitizenI?): AccessResponse where S : EntityCreatedBy<C>, S : OpinionI = when {
        citizen == null -> denied("You must be connected to delete opinion", "opinion.delete.notConnected")
        subject.createdBy.id != citizen.id -> denied("You can only delete your opinions", "opinion.delete.notYours")
        else -> granted()
    }
}

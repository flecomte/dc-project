package fr.dcproject.component.constitution

import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.security.AccessControl
import fr.dcproject.security.AccessResponse
import fr.postgresjson.entity.EntityCreatedBy
import fr.postgresjson.entity.EntityDeletedAt

class ConstitutionAccessControl : AccessControl() {
    fun canCreate(subject: ConstitutionS, citizen: CitizenI?): AccessResponse = when {
        citizen == null -> denied("You must be connected to create constitution", "constitution.create.notConnected")
        else -> granted()
    }

    fun <S : ConstitutionSimple<*, *>> canView(subjects: List<S>, citizen: CitizenI?): AccessResponse =
        canAll(subjects) { canView(it, citizen) }

    fun <S> canView(subject: S, citizen: CitizenI?): AccessResponse where S: EntityDeletedAt, S: ConstitutionS = when {
        subject.isDeleted() -> denied("You cannot view a deleted constitution", "constitution.view.deleted")
        else -> granted()
    }

    fun <S> canDelete(subject: S, citizen: CitizenI?): AccessResponse where S : EntityCreatedBy<CitizenI>, S : ConstitutionRef = when {
        citizen == null -> denied("You must be connected to delete constitution", "constitution.delete.notConnected")
        subject.createdBy.id != citizen.id -> denied("You cannot delete the constitution of other citizen", "constitution.delete.otherCitizen")
        else -> granted()
    }

    fun <S> canUpdate(subject: S, citizen: CitizenI?): AccessResponse where S : EntityCreatedBy<CitizenI>, S : ConstitutionRef = when {
        citizen == null -> denied("You must be connected to update constitution", "constitution.update.notConnected")
        subject.createdBy.id != citizen.id -> denied("You cannot update the constitution of other citizen", "constitution.update.otherCitizen")
        else -> granted()
    }
}

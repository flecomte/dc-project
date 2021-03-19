package fr.dcproject.component.constitution

import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.DeletedAt
import fr.dcproject.common.security.AccessControl
import fr.dcproject.common.security.AccessResponse
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.constitution.database.ConstitutionForListing
import fr.dcproject.component.constitution.database.ConstitutionI
import fr.dcproject.component.constitution.database.ConstitutionRef

class ConstitutionAccessControl : AccessControl() {
    fun canCreate(subject: ConstitutionI, citizen: CitizenI?): AccessResponse = when {
        citizen == null -> denied("You must be connected to create constitution", "constitution.create.notConnected")
        else -> granted()
    }

    fun canView(subjects: List<ConstitutionForListing>, citizen: CitizenI?): AccessResponse =
        canAll(subjects) { canView(it, citizen) }

    fun <S> canView(subject: S, citizen: CitizenI?): AccessResponse where S : DeletedAt, S : ConstitutionI = when {
        subject.isDeleted() -> denied("You cannot view a deleted constitution", "constitution.view.deleted")
        else -> granted()
    }

    fun <S> canDelete(subject: S, citizen: CitizenI?): AccessResponse where S : CreatedBy<CitizenI>, S : ConstitutionRef = when {
        citizen == null -> denied("You must be connected to delete constitution", "constitution.delete.notConnected")
        subject.createdBy.id != citizen.id -> denied("You cannot delete the constitution of other citizen", "constitution.delete.otherCitizen")
        else -> granted()
    }

    fun <S> canUpdate(subject: S, citizen: CitizenI?): AccessResponse where S : CreatedBy<CitizenI>, S : ConstitutionRef = when {
        citizen == null -> denied("You must be connected to update constitution", "constitution.update.notConnected")
        subject.createdBy.id != citizen.id -> denied("You cannot update the constitution of other citizen", "constitution.update.otherCitizen")
        else -> granted()
    }
}

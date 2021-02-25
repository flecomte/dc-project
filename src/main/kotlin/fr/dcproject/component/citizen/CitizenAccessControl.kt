package fr.dcproject.component.citizen

import fr.dcproject.common.entity.DeletedAt
import fr.dcproject.common.security.AccessControl
import fr.dcproject.common.security.AccessResponse

class CitizenAccessControl : AccessControl() {
    fun <S> canView(subjects: List<S>, connectedCitizen: CitizenI?): AccessResponse where S : CitizenI, S : DeletedAt =
        canAll(subjects) { canView(it, connectedCitizen) }

    fun <S> canView(subject: S, connectedCitizen: CitizenI?): AccessResponse where S : CitizenI, S : DeletedAt {
        if (connectedCitizen == null) return denied("You must be connected to view citizen", "citizen.view.connected")
        return if (subject.isDeleted()) denied("You cannot view a deleted citizen", "citizen.view.deleted")
        else granted()
    }

    fun <S : CitizenI> canUpdate(subject: S, connectedCitizen: CitizenI?): AccessResponse {
        if (connectedCitizen == null) return denied("You must be connected to update Citizen", "citizen.update.notConnected")
        return if (subject.id == connectedCitizen.id) granted() else denied("You can only update your citizen", "citizen.update.notYours")
    }

    fun <S : CitizenI> canChangePassword(subject: S, connectedCitizen: CitizenI?): AccessResponse {
        if (connectedCitizen == null) return denied("You must be connected to change your password", "citizen.changePassword.notConnected")
        return if (subject.id == connectedCitizen.id) granted() else denied("You can only change your password", "citizen.password.notYours")
    }
}

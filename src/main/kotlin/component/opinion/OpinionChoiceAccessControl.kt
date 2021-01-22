package fr.dcproject.component.opinion

import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.opinion.entity.OpinionChoice
import fr.dcproject.security.AccessControl
import fr.dcproject.security.AccessResponse

class OpinionChoiceAccessControl : AccessControl() {
    fun canView(subjects: List<OpinionChoice>, citizen: CitizenI?): AccessResponse =
        canAll(subjects) { canView(it, citizen) }

    fun canView(subject: OpinionChoice, citizen: CitizenI?): AccessResponse {
        return granted()
    }
}

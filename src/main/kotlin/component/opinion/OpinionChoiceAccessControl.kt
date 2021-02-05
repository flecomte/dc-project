package fr.dcproject.component.opinion

import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.opinion.entity.OpinionChoiceI
import fr.dcproject.security.AccessControl
import fr.dcproject.security.AccessResponse

class OpinionChoiceAccessControl : AccessControl() {
    fun canView(subjects: List<OpinionChoiceI>, citizen: CitizenI?): AccessResponse =
        canAll(subjects) { canView(it, citizen) }

    fun canView(subject: OpinionChoiceI, citizen: CitizenI?): AccessResponse {
        return granted()
    }
}

package fr.dcproject.component.opinion

import fr.dcproject.common.security.AccessControl
import fr.dcproject.common.security.AccessResponse
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.opinion.database.OpinionChoiceI

class OpinionChoiceAccessControl : AccessControl() {
    fun canView(subjects: List<OpinionChoiceI>, citizen: CitizenI?): AccessResponse =
        canAll(subjects) { canView(it, citizen) }

    fun canView(subject: OpinionChoiceI, citizen: CitizenI?): AccessResponse {
        return granted()
    }
}

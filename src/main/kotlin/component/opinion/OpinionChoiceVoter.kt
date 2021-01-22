package fr.dcproject.component.opinion

import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.opinion.entity.OpinionChoice
import fr.dcproject.voter.Voter
import fr.dcproject.voter.VoterResponse

class OpinionChoiceVoter : Voter() {
    fun canView(subjects: List<OpinionChoice>, citizen: CitizenI?): VoterResponse =
        canAll(subjects) { canView(it, citizen) }

    fun canView(subject: OpinionChoice, citizen: CitizenI?): VoterResponse {
        return granted()
    }
}

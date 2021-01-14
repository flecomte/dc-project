package fr.dcproject.voter

import fr.ktorVoter.ActionI
import fr.ktorVoter.VoterException

class NoSubjectDefinedException(action: ActionI) : VoterException("""No subject for action "$action" is defined""")
package fr.dcproject.voter

import fr.ktorVoter.ActionI
import fr.ktorVoter.VoterException

class NoRuleDefinedException(action: ActionI) : VoterException("""No rule for action "$action" is defined""")
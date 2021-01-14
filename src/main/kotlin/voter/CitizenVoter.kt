package fr.dcproject.security.voter

import fr.dcproject.entity.CitizenBasicI
import fr.dcproject.entity.CitizenWithUserI
import fr.dcproject.user
import fr.dcproject.voter.NoRuleDefinedException
import fr.dcproject.voter.NoSubjectDefinedException
import fr.ktorVoter.*
import io.ktor.application.*
import io.ktor.locations.*

@KtorExperimentalLocationsAPI
class CitizenVoter : Voter<ApplicationCall> {
    enum class Action : ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE,
        CHANGE_PASSWORD
    }

    override fun invoke(action: Any, context: ApplicationCall, subject: Any?): VoterResponseI {
        if (!((action is Action)
            && (subject is CitizenBasicI?))) return abstain()

        val user = context.user
        if (action == Action.CREATE && user != null) {
            return granted()
        }

        if (action == Action.VIEW) {
            if (user == null) return denied("You must be connected to view citizen", "citizen.view.connected")
            if (subject is CitizenBasicI) {
                return if (subject.isDeleted()) denied("You cannot view a deleted citizen", "citizen.view.deleted")
                else granted()
            }
            throw NoRuleDefinedException(action)
        }

        if (action == Action.DELETE) {
            return denied("You can never deleted a citizen", "citizen.delete.never")
        }

        if (action == Action.UPDATE) {
            if (user == null) return denied("You must be connected to update Citizen", "citizen.update.notConnected")
            if (subject !is CitizenWithUserI) throw NoSubjectDefinedException(action)
            return if (subject.user.id == user.id) granted() else denied("You can only update your citizen", "citizen.update.notYours")
        }

        if (action == Action.CHANGE_PASSWORD && user != null && subject is CitizenBasicI) {
            val userToChange = subject.user
            return if (user.id == userToChange.id) {
                granted()
            } else {
                denied("You can only change your password", "citizen.password.notYours")
            }
        }

       throw NoRuleDefinedException(action)
    }
}

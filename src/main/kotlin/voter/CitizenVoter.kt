package fr.dcproject.security.voter

import fr.dcproject.entity.CitizenBasicI
import fr.dcproject.entity.UserI
import fr.dcproject.user
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.Voter
import io.ktor.application.ApplicationCall
import io.ktor.locations.KtorExperimentalLocationsAPI

@KtorExperimentalLocationsAPI
class CitizenVoter : Voter {
    enum class Action : ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE,
        CHANGE_PASSWORD
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return (action is Action)
            .and(subject is CitizenBasicI?)
    }

    override fun vote(action: ActionI, call: ApplicationCall, subject: Any?): Vote {
        val user = call.user
        if (action == Action.CREATE && user != null) {
            return Vote.GRANTED
        }

        if (action == Action.VIEW) {
            if (user == null) return Vote.DENIED
            if (subject is CitizenBasicI) {
                return if (subject.isDeleted()) Vote.DENIED
                else Vote.GRANTED
            }
            return Vote.DENIED
        }

        if (action == Action.DELETE) {
            return Vote.DENIED
        }

        if (action == Action.UPDATE &&
            user is UserI &&
            subject is CitizenBasicI &&
            subject.user.id == user.id
        ) {
            return Vote.GRANTED
        }

        if (action == Action.CHANGE_PASSWORD && user != null && subject is CitizenBasicI) {
            val userToChange = subject.user
            return if (user.id == userToChange.id) {
                Vote.GRANTED
            } else {
                Vote.ABSTAIN
            }
        }

        if (action is Action) {
            return Vote.DENIED
        }

        return Vote.ABSTAIN
    }
}

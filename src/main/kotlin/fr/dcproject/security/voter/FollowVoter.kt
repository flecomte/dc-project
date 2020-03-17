package fr.dcproject.security.voter

import io.ktor.application.ApplicationCall
import fr.dcproject.entity.Follow as FollowEntity
import fr.dcproject.entity.User as UserEntity

class FollowVoter : Voter {
    enum class Action : ActionI {
        CREATE,
        DELETE,
        VIEW
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return (action is Action)
            .and(subject is FollowEntity<*>?)
    }

    override fun vote(action: ActionI, call: ApplicationCall, subject: Any?): Vote {
        val user = call.user
        if (action == Action.CREATE) {
            return if (user != null) Vote.GRANTED
            else Vote.DENIED
        }

        if (action == Action.DELETE) {
            return if (user != null) Vote.GRANTED
            else Vote.DENIED
        }

        if (action == Action.VIEW) {
            if (subject is FollowEntity<*>) {
                return voteView(user, subject)
            }
            return Vote.DENIED
        }

        return Vote.ABSTAIN
    }

    private fun voteView(user: UserEntity?, subject: FollowEntity<*>): Vote {
        return if ((user != null && subject.createdBy.user.id == user.id) || !subject.createdBy.followAnonymous) Vote.GRANTED
        else Vote.DENIED
    }
}

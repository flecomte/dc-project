package fr.dcproject.security.voter

import fr.dcproject.user
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.Voter
import io.ktor.application.ApplicationCall
import fr.dcproject.entity.Follow as FollowEntity
import fr.dcproject.entity.User as UserEntity

class FollowVoter : Voter<ApplicationCall> {
    enum class Action : ActionI {
        CREATE,
        DELETE,
        VIEW
    }

    override fun invoke(action: Any, context: ApplicationCall, subject: Any?): Vote {
        if (!((action is Action)
            && (subject is FollowEntity<*>?))) return Vote.ABSTAIN

        val user = context.user
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

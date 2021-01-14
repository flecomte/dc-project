package fr.dcproject.security.voter

import fr.dcproject.citizenOrNull
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.entity.FollowI
import fr.dcproject.voter.NoSubjectDefinedException
import fr.ktorVoter.*
import io.ktor.application.*
import fr.dcproject.entity.Follow as FollowEntity

class FollowVoter : Voter<ApplicationCall> {
    enum class Action : ActionI {
        CREATE,
        DELETE,
        VIEW
    }

    override fun invoke(action: Any, context: ApplicationCall, subject: Any?): VoterResponseI {
        if (action !is Action) return abstain()
        if (subject !is FollowI) throw NoSubjectDefinedException(action)

        val citizen = context.citizenOrNull
        if (action == Action.CREATE) {
            return if (citizen == null) denied("You must be connected to follow", "follow.create.notConnected")
            else granted()
        }

        if (action == Action.DELETE) {
            return if (citizen == null) denied("You must be connected to unfollow", "follow.delete.notConnected")
            else granted()
        }

        if (action == Action.VIEW) {
            if (subject is FollowEntity<*>) {
                return voteView(citizen, subject)
            }
            throw NoSubjectDefinedException(action)
        }

        return abstain()
    }

    private fun voteView(citizen: CitizenI?, subject: FollowEntity<*>): VoterResponseI {
        return if ((citizen != null && subject.createdBy.id == citizen.id) || !subject.createdBy.followAnonymous) granted()
        else denied("You cannot view an anonymous follow", "follow.view.anonymous")
    }
}

package fr.dcproject.security.voter

import fr.dcproject.citizenOrNull
import fr.dcproject.entity.VoteForUpdateI
import fr.dcproject.entity.VoteI
import fr.dcproject.voter.NoSubjectDefinedException
import fr.ktorVoter.*
import fr.postgresjson.entity.EntityDeletedAt
import io.ktor.application.*
import fr.dcproject.entity.Vote as VoteEntity

class VoteVoter : Voter<ApplicationCall> {
    enum class Action : ActionI {
        CREATE,
        VIEW
    }

    override fun invoke(action: Any, context: ApplicationCall, subject: Any?): VoterResponseI {
        if ((action is Action && subject == null)) throw NoSubjectDefinedException(action)
        if (!(action is Action && subject is VoteI)) return abstain()

        val citizen = context.citizenOrNull ?: return denied("You must be connected for vote", "vote.connected")

        if (action == Action.CREATE) {
            if (subject !is VoteForUpdateI<*, *>) throw NoSubjectDefinedException(action)
            subject.target.let {
                if (it is EntityDeletedAt) {
                    if (it.isDeleted()) return denied("You cannot vote on deleted target", "vote.create.isDeleted")
                } else  {
                    throw NoSubjectDefinedException(action)
                }
            }
            return granted()
        }

        if (action == Action.VIEW) {
            if (subject is VoteEntity<*>) {
                return if (subject.createdBy.id != citizen.id) {
                    denied("You can view only your votes", "vote.view")
                } else {
                    granted()
                }
            } else {
                throw NoSubjectDefinedException(action)
            }
        }

        return abstain()
    }
}

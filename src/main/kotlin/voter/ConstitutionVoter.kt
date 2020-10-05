package fr.dcproject.security.voter

import fr.dcproject.entity.Comment
import fr.dcproject.entity.ConstitutionSimple
import fr.dcproject.entity.UserI
import fr.dcproject.user
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.Voter
import io.ktor.application.ApplicationCall
import fr.dcproject.entity.Vote as VoteEntity

class ConstitutionVoter : Voter<ApplicationCall> {
    enum class Action : ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE
    }

    override fun invoke(action: Any, context: ApplicationCall, subject: Any?): Vote {
        if(!((action is Action || action is CommentVoter.Action || action is VoteVoter.Action)
            && (subject is ConstitutionSimple<*, *>? || subject is VoteEntity<*> || subject is Comment<*>))) return Vote.ABSTAIN

        val user = context.user
        if (action == Action.CREATE && user != null) {
            return Vote.GRANTED
        }

        if (action == Action.VIEW) {
            if (subject is ConstitutionSimple<*, *>) {
                return if (subject.isDeleted()) Vote.DENIED
                else Vote.GRANTED
            }
            return Vote.DENIED
        }

        if (action == Action.DELETE && user is UserI && subject is ConstitutionSimple<*, *> && subject.createdBy.user.id == user.id) {
            return Vote.GRANTED
        }

        if (action == Action.UPDATE && user is UserI && subject is ConstitutionSimple<*, *> && subject.createdBy.user.id == user.id) {
            return Vote.GRANTED
        }

        if (action is CommentVoter.Action) return voteForComment(action)
        if (action is VoteVoter.Action) return voteForVote(action, subject)

        if (action is Action) {
            return Vote.DENIED
        }

        return Vote.ABSTAIN
    }

    private fun voteForVote(action: VoteVoter.Action, subject: Any?): Vote {
        if (action == VoteVoter.Action.CREATE && subject is VoteEntity<*>) {
            val target = subject.target
            if (target !is ConstitutionSimple<*, *>) {
                return Vote.ABSTAIN
            }
            if (target.isDeleted()) {
                return Vote.DENIED
            }
        }
        return Vote.ABSTAIN
    }

    private fun voteForComment(action: CommentVoter.Action): Vote {
        if (action == CommentVoter.Action.CREATE) {
            return Vote.GRANTED
        }

        if (action == CommentVoter.Action.VIEW) {
            return Vote.GRANTED
        }

        return Vote.ABSTAIN
    }
}

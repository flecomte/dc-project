package fr.dcproject.security.voter

import fr.dcproject.component.comment.generic.CommentForView
import fr.dcproject.entity.ConstitutionSimple
import fr.dcproject.component.auth.UserI
import fr.dcproject.user
import fr.dcproject.voter.NoRuleDefinedException
import fr.dcproject.voter.NoSubjectDefinedException
import fr.ktorVoter.*
import io.ktor.application.*
import fr.dcproject.entity.Vote as VoteEntity

class ConstitutionVoter : Voter<ApplicationCall> {
    enum class Action : ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE
    }

    override fun invoke(action: Any, context: ApplicationCall, subject: Any?): VoterResponseI {
        if (!((action is Action || action is VoteVoter.Action) &&
            (subject is ConstitutionSimple<*, *>? || subject is VoteEntity<*> || subject is CommentForView<*, *>))) return abstain()

        val user = context.user
        if (action == Action.CREATE && user != null) {
            return granted()
        }

        if (action == Action.VIEW) {
            if (subject is ConstitutionSimple<*, *>) {
                return if (subject.isDeleted()) denied("You cannot view a deleted constitution", "constitution.view.deleted")
                else granted()
            }
            throw NoSubjectDefinedException(action as ActionI)
        }

        if (action == Action.DELETE && user is UserI && subject is ConstitutionSimple<*, *> && subject.createdBy.user.id == user.id) {
            return granted()
        }

        if (action == Action.UPDATE && user is UserI && subject is ConstitutionSimple<*, *> && subject.createdBy.user.id == user.id) {
            return granted()
        }

        if (action is VoteVoter.Action) return voteForVote(action, subject)

        if (action is Action) {
            throw NoRuleDefinedException(action)
        }

        return abstain()
    }

    private fun voteForVote(action: VoteVoter.Action, subject: Any?): VoterResponseI {
        if (action == VoteVoter.Action.CREATE && subject is VoteEntity<*>) {
            val target = subject.target
            if (target !is ConstitutionSimple<*, *>) {
                return abstain()
            }
            if (target.isDeleted()) {
                return denied("You cannot vote a deleted constitution", "constitution.vote.deleted")
            }
        }
        return abstain()
    }
}

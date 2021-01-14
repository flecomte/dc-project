package fr.dcproject.security.voter

import fr.dcproject.component.article.ArticleAuthI
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.entity.Opinion
import fr.dcproject.user
import fr.dcproject.voter.NoRuleDefinedException
import fr.dcproject.voter.NoSubjectDefinedException
import fr.ktorVoter.*
import io.ktor.application.*

class OpinionVoter : Voter<ApplicationCall> {
    enum class Action : ActionI {
        CREATE,
        VIEW,
        DELETE
    }

    override fun invoke(action: Any, context: ApplicationCall, subject: Any?): VoterResponseI {
        if (!((action is Action) &&
            (subject is Opinion<*>? || subject is ArticleAuthI<*>))) return abstain()

        val user = context.user
        if (action == Action.CREATE) {
            if (user == null) return denied("You must be connected to make an opinion", "opinion.create.notConnected")
            if (subject is ArticleAuthI<*> && !subject.isDeleted()) return granted()
            if (subject is Opinion<*> && subject.createdBy.user.id == user.id) return granted()

            throw NoSubjectDefinedException(action)
        }

        if (action == Action.VIEW) {
            return if (subject is Opinion<*> || subject is ArticleForView) granted() else throw NoSubjectDefinedException(action)
        }

        if (action == Action.DELETE) {
            if (user == null) return denied("You must be connected to delete opinion", "opinion.delete.notConnected")
            if (subject !is Opinion<*>) throw NoSubjectDefinedException(action)
            return if (subject.createdBy.user.id == user.id) granted() else denied("You can only delete your opinions", "opinion.delete.notYours")
        }

        if (action is Action) {
            throw NoRuleDefinedException(action)
        }

        return abstain()
    }
}

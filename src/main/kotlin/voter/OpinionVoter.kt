package fr.dcproject.security.voter

import fr.dcproject.entity.Article
import fr.dcproject.entity.ArticleAuthI
import fr.dcproject.entity.Opinion
import fr.dcproject.user
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.Vote.Companion.toVote
import fr.ktorVoter.Voter
import io.ktor.application.ApplicationCall

class OpinionVoter : Voter<ApplicationCall> {
    enum class Action : ActionI {
        CREATE,
        VIEW,
        DELETE
    }

    override fun invoke(action: Any, context: ApplicationCall, subject: Any?): Vote {
        if (!((action is Action)
            && (subject is Opinion<*>? || subject is ArticleAuthI<*>))) return Vote.ABSTAIN

        val user = context.user
        if (action == Action.CREATE) {
            return toVote {
                user != null && (
                    (subject is ArticleAuthI<*> && !subject.isDeleted()) ||
                    (subject is Opinion<*> && subject.createdBy.user.id == user.id)
                )
            }
        }

        if (action == Action.VIEW) {
            return toVote { subject is Opinion<*> || subject is Article }
        }

        if (action == Action.DELETE) {
            return toVote {
                subject is Opinion<*> &&
                user != null &&
                subject.createdBy.user.id == user.id
            }
        }

        return Vote.ABSTAIN
    }
}

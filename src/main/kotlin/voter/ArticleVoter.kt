package fr.dcproject.security.voter

import fr.dcproject.citizenOrNull
import fr.dcproject.entity.ArticleAuthI
import fr.dcproject.entity.ArticleForUpdateI
import fr.dcproject.entity.ArticleI
import fr.dcproject.entity.Citizen as CitizenEntity
import fr.dcproject.entity.CitizenI
import fr.dcproject.entity.UserI
import fr.dcproject.repository.Article as ArticleRepo
import fr.dcproject.user
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.Vote.Companion.toVote
import fr.ktorVoter.Voter
import io.ktor.application.ApplicationCall
import org.koin.core.KoinComponent
import org.koin.core.inject
import fr.dcproject.entity.Comment as CommentEntity
import fr.dcproject.entity.Vote as VoteEntity

class ArticleVoter(private val articleRepo: ArticleRepo) : Voter<ApplicationCall> {
    enum class Action : ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE
    }

    override fun invoke(action: Any, context: ApplicationCall, subject: Any?): Vote {
        if (!((action is Action || action is CommentVoter.Action || action is VoteVoter.Action)
            && (subject is ArticleI? || subject is VoteEntity<*> || subject is CommentEntity<*>))
        ) return Vote.ABSTAIN

        val user = context.user
        if (action == Action.CREATE && user is UserI) return Vote.GRANTED
        if (action == Action.VIEW) return view(subject, user)
        if (action == Action.DELETE) return delete(subject, user)
        if (action == Action.UPDATE) return update(subject, context.citizenOrNull)
        if (action is CommentVoter.Action) return voteForComment(action, subject)
        if (action is VoteVoter.Action) return voteForVote(action, subject)
        if (action is Action) return Vote.DENIED

        return Vote.ABSTAIN
    }

    private fun view(subject: Any?, user: UserI?): Vote {
        if (subject is ArticleAuthI<*>) {
            return if (subject.isDeleted()) Vote.DENIED
            else if (subject.draft && (user == null || subject.createdBy.user.id != user.id)) Vote.DENIED
            else Vote.GRANTED
        }
        return Vote.DENIED
    }

    private fun delete(subject: Any?, user: UserI?): Vote {
        if (subject is ArticleAuthI<*>) {
            if (user is UserI && subject.createdBy.user.id == user.id) {
                return Vote.GRANTED
            }
        }
        return Vote.DENIED
    }

    private fun update(subject: Any?, citizen: CitizenEntity?): Vote {
        /* The new Article must by created by the same citizen of the connected citizen */
        if (subject is ArticleForUpdateI && citizen is CitizenI && subject.createdBy.id == citizen.id) {
            /* The creator must be the same of the creator of preview version of article */
            return toVote {
                articleRepo
                    .findVerionsByVersionsId(1, 1, subject.versionId)
                    .result.first()
                    .createdBy.id == citizen.id
            }
        }
        return Vote.DENIED
    }

    private fun voteForVote(action: VoteVoter.Action, subject: Any?): Vote {
        if (action == VoteVoter.Action.CREATE && subject is VoteEntity<*>) {
            val target = subject.target
            if (target is ArticleAuthI<*>) {
                if (target.isDeleted()) {
                    return Vote.DENIED
                }
            } else if (target is ArticleI) {
                return Vote.DENIED
            }
        }
        return Vote.ABSTAIN
    }

    private fun voteForComment(action: CommentVoter.Action, subject: Any?): Vote {
        if (subject is CommentEntity<*>) {
            val target = subject.target
            if (target is ArticleAuthI<*>) {
                if (target.isDeleted()) {
                    return Vote.DENIED
                }
            } else if (target is ArticleI) {
                return Vote.DENIED
            }
            if (action == CommentVoter.Action.CREATE) {
                return Vote.GRANTED
            }

            if (action == CommentVoter.Action.VIEW) {
                return Vote.GRANTED
            }
        } else {
            return Vote.DENIED
        }

        return Vote.ABSTAIN
    }
}

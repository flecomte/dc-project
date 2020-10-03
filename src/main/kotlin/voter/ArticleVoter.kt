package fr.dcproject.security.voter

import fr.dcproject.citizenOrNull
import fr.dcproject.entity.ArticleAuthI
import fr.dcproject.entity.ArticleForUpdate
import fr.dcproject.entity.ArticleI
import fr.dcproject.entity.Citizen as CitizenEntity
import fr.dcproject.entity.CitizenI
import fr.dcproject.entity.UserI
import fr.dcproject.repository.Article as ArticleRepo
import fr.dcproject.user
import fr.ktorVoter.ActionI
import fr.ktorVoter.Vote
import fr.ktorVoter.Voter
import fr.ktorVoter.checkClass
import io.ktor.application.ApplicationCall
import org.koin.core.KoinComponent
import org.koin.core.inject
import fr.dcproject.entity.Comment as CommentEntity
import fr.dcproject.entity.Vote as VoteEntity

class ArticleVoter : Voter, KoinComponent {
    private val articleRepo: ArticleRepo by inject()
    enum class Action : ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return (action is Action || action is CommentVoter.Action || action is VoteVoter.Action)
            .and(subject is ArticleI? || subject is VoteEntity<*> || subject is CommentEntity<*>)
    }

    override fun vote(action: ActionI, call: ApplicationCall, subject: Any?): Vote {
        val user = call.user
        if (action == Action.CREATE && user is UserI) return Vote.GRANTED
        if (action == Action.VIEW) return view(subject, user)
        if (action == Action.DELETE) return delete(subject, user)
        if (action == Action.UPDATE) return update(subject, call.citizenOrNull)
        if (action is CommentVoter.Action) return voteForComment(action, subject)
        if (action is VoteVoter.Action) return voteForVote(action, subject)
        if (action is Action) return Vote.DENIED

        return Vote.ABSTAIN
    }

    private fun view(subject: Any?, user: UserI?): Vote {
        checkClass(ArticleAuthI::class, subject)
        if (subject is ArticleAuthI<*>) {
            return if (subject.isDeleted()) Vote.DENIED
            else if (subject.draft && (user == null || subject.createdBy.user.id != user.id)) Vote.DENIED
            else Vote.GRANTED
        }
        return Vote.DENIED
    }

    private fun delete(subject: Any?, user: UserI?): Vote {
        checkClass(ArticleAuthI::class, subject)
        if (subject is ArticleAuthI<*>) {
            if (user is UserI && subject.createdBy.user.id == user.id) {
                return Vote.GRANTED
            }
        }
        return Vote.DENIED
    }

    private fun update(subject: Any?, citizen: CitizenEntity?): Vote {
        checkClass(ArticleForUpdate::class, subject)
        if (subject is ArticleForUpdate) {
            /* The new Article must by created by the same citizen of the connected citizen */
            if (citizen is CitizenI && subject.createdBy.id == citizen.id) {
                /* The creator must be the same of the creator of preview version of article */
                if(articleRepo.findVerionsByVersionsId(1, 1, subject.versionId).result.first().createdBy.id == citizen.id) {
                    return Vote.GRANTED
                }
                return Vote.DENIED
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

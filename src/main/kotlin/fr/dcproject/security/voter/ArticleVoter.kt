package fr.dcproject.security.voter

import fr.dcproject.entity.User
import io.ktor.application.ApplicationCall
import fr.dcproject.entity.Article as ArticleEntity
import fr.dcproject.entity.Comment as CommentEntity
import fr.dcproject.entity.Vote as VoteEntity

class ArticleVoter: Voter {
    enum class Action: ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE
    }

    override fun supports(action: ActionI, call: ApplicationCall, subject: Any?): Boolean {
        return (action is Action || action is CommentVoter.Action || action is VoteVoter.Action)
               &&
               (subject is List<*> || subject is ArticleEntity? || subject is VoteEntity<*> || subject is CommentEntity<*>)
    }

    override fun vote(action: ActionI, call: ApplicationCall, subject: Any?): Vote {
        val user = call.user
        if (action == Action.CREATE && user is User) {
            return Vote.GRANTED
        }

        if (action == Action.VIEW) {
            if (subject is ArticleEntity) {
                return if (subject.isDeleted()) Vote.DENIED
                else Vote.GRANTED
            }
            if (subject is List<*>) {
                subject.forEach {
                    if (it !is ArticleEntity || it.isDeleted()) {
                        return Vote.DENIED
                    }
                }
                return Vote.GRANTED
            }
            return Vote.DENIED
        }

        if (action is CommentVoter.Action) return voteForComment(action)
        if (action is VoteVoter.Action) return voteForVote(action, subject)

        if (subject is ArticleEntity) {
            if (action == Action.DELETE && user is User && subject.createdBy?.userId == user.id) {
                return Vote.GRANTED
            }

            if (action == Action.UPDATE && user is User && subject.createdBy?.userId == user.id) {
                return Vote.GRANTED
            }

            return Vote.DENIED
        }

        if (action is Action) {
            return Vote.DENIED
        }

        return Vote.ABSTAIN
    }

    private fun voteForVote(action: VoteVoter.Action, subject: Any?): Vote {
        if (action == VoteVoter.Action.CREATE && subject is VoteEntity<*>) {
            val target = subject.target
            if (target !is ArticleEntity) {
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

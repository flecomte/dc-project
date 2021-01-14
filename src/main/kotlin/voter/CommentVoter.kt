package fr.dcproject.security.voter

import fr.dcproject.citizenOrNull
import fr.dcproject.entity.CommentForUpdate
import fr.dcproject.entity.CommentForView
import fr.dcproject.entity.CommentI
import fr.dcproject.voter.NoRuleDefinedException
import fr.dcproject.voter.NoSubjectDefinedException
import fr.ktorVoter.*
import fr.postgresjson.entity.EntityDeletedAt
import io.ktor.application.*

class CommentVoter : Voter<ApplicationCall> {
    enum class Action : ActionI {
        CREATE,
        UPDATE,
        VIEW,
        DELETE
    }

    override fun invoke(action: Any, context: ApplicationCall, subject: Any?): VoterResponseI {
        if (!(action is Action && subject is CommentI?)) return abstain()

        val citizen = context.citizenOrNull

        if (subject == null) {
            throw NoSubjectDefinedException(action)
        }

        if (action == Action.CREATE) {
            return when {
                citizen == null -> denied("You must be connected to create user", "comment.create.notConnected")
                subject !is CommentForUpdate<*, *> -> throw NoSubjectDefinedException(action)
                subject.createdBy.id != citizen.id -> denied("You cannot create a comment with other user than yours", "comment.create.wrongUser")
                subject.parent?.isDeleted() ?: false -> denied("You cannot create a comment on deleted parent", "comment.create.deletedParent")
                subject.target.let { it is EntityDeletedAt && it.isDeleted() } -> denied("You cannot create a comment on deleted target", "comment.create.deletedTarget")
                else -> granted()
            }
        }

        if (action == Action.VIEW) {
            return when {
                subject !is CommentForView<*, *> -> throw NoSubjectDefinedException(action)
                subject.isDeleted() -> denied("Your cannot view a deleted comment", "comment.view.deleted")
                else -> granted()
            }
        }

        if (action == Action.UPDATE) {
            if (citizen == null) return denied("You must be connected to update comment", "comment.update.notConnected")
            return when {
                subject !is CommentForUpdate<*, *> -> throw NoSubjectDefinedException(action)
                citizen.id == subject.createdBy.id -> granted()
                else -> denied("You cannot update another user of yours", "comment.update.notYours")
            }
        }

        if (action == Action.DELETE) {
            return denied("A comment can never be deleted", "comment.deleted.never")
        }

        throw NoRuleDefinedException(action)
    }
}

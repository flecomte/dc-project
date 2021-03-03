package fr.dcproject.component.comment.generic

import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.DeletedAt
import fr.dcproject.common.entity.HasTarget
import fr.dcproject.common.security.AccessControl
import fr.dcproject.common.security.AccessResponse
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.comment.generic.database.CommentI
import fr.dcproject.component.comment.generic.database.CommentWithParentI

class CommentAccessControl : AccessControl() {
    fun <S> canView(subjects: List<S>, citizen: CitizenI?): AccessResponse
    where S : CommentI,
          S : DeletedAt = canAll(subjects) { canView(it, citizen) }

    fun <S> canView(subject: S, citizen: CitizenI?): AccessResponse
    where S : CommentI,
          S : DeletedAt = when {
        subject.isDeleted() -> denied("Your cannot view a deleted comment", "comment.view.deleted")
        else -> granted()
    }

    fun <S, CR : CitizenI> canCreate(subject: S, citizen: CitizenI?): AccessResponse
    where S : CommentI,
          S : CreatedBy<CR>,
          S : CommentWithParentI<*>,
          S : HasTarget<*> = when {
        citizen == null -> denied("You must be connected to create user", "comment.create.notConnected")
        subject.createdBy.id != citizen.id -> denied("You cannot create a comment with other user than yours", "comment.create.wrongUser")
        subject.parent?.isDeleted() ?: false -> denied("You cannot create a comment on deleted parent", "comment.create.deletedParent")
        subject.target.let { it is DeletedAt && it.isDeleted() } -> denied("You cannot create a comment on deleted target", "comment.create.deletedTarget")
        else -> granted()
    }

    fun <S, CR : CitizenI> canUpdate(subject: S, citizen: CitizenI?): AccessResponse
    where S : CommentI,
          S : CreatedBy<CR> = when {
        citizen == null -> denied("You must be connected to update comment", "comment.update.notConnected")
        citizen.id != subject.createdBy.id -> denied("You cannot update another user of yours", "comment.update.notYours")
        else -> granted()
    }
}

package fr.dcproject.component.comment.generic.database

import fr.dcproject.common.entity.CreatedAt
import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.DeletedAt
import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.ExtraI
import fr.dcproject.common.entity.HasTarget
import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.common.entity.UpdatedAt
import fr.dcproject.component.citizen.database.CitizenCreatorI
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.vote.entity.Votable
import fr.dcproject.component.vote.entity.VotableImp
import org.joda.time.DateTime
import java.util.UUID

data class CommentForView<T : TargetI, C : CitizenCreatorI>(
    override val id: UUID = UUID.randomUUID(),
    override val createdBy: C,
    override val target: T,
    override var content: String,
    override val parent: CommentParent<T>? = null,
    val childrenCount: Int? = null,
    override val deletedAt: DateTime? = null
) : ExtraI<T, C>,
    CommentWithParentI<T>,
    CommentForUpdate<T, C>(id, createdBy, target, content, parent, deletedAt),
    CommentWithTargetI<T>,
    CreatedBy<C> by CreatedBy.Imp(createdBy),
    UpdatedAt by UpdatedAt.Imp(),
    DeletedAt,
    Votable by VotableImp(),
    TargetI {
    constructor(
        createdBy: C,
        parent: CommentParent<T>,
        content: String
    ) : this(
        createdBy = createdBy,
        parent = parent,
        target = parent.target,
        content = content
    )
}

open class CommentForUpdate<T : TargetI, C : CitizenI>(
    override val id: UUID = UUID.randomUUID(),
    override val createdBy: C,
    override val target: T,
    open var content: String,
    override val parent: CommentParentI<T>? = null,
    override val deletedAt: DateTime? = null
) : CommentParentI<T> by CommentParent(id, deletedAt, target),
    CommentWithParentI<T>,
    ExtraI<T, C>,
    CommentWithTargetI<T>,
    CreatedAt by CreatedAt.Imp(),
    CreatedBy<C>,
    DeletedAt,
    TargetI {
    constructor(
        createdBy: C,
        parent: CommentParentI<T>,
        content: String,
        id: UUID? = null,
    ) : this(
        createdBy = createdBy,
        parent = parent,
        target = parent.target,
        content = content,
        id = id ?: UUID.randomUUID(),
    )
}

data class CommentParent<T : TargetI>(
    override val id: UUID,
    override val deletedAt: DateTime?,
    override val target: T
) : CommentRef(id),
    CommentParentI<T>

sealed interface CommentParentI<T : TargetI> : CommentI, DeletedAt, CommentWithTargetI<T>

interface CommentWithTargetI<T : TargetI> : CommentI, TargetI, HasTarget<T>

interface CommentWithParentI<T : TargetI> {
    val parent: CommentParentI<T>?
}

open class CommentRef(id: UUID = UUID.randomUUID()) : CommentI, TargetRef(id)

sealed interface CommentI : EntityI

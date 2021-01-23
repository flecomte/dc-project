package fr.dcproject.component.comment.generic

import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.vote.entity.Votable
import fr.dcproject.component.vote.entity.VotableImp
import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.ExtraI
import fr.dcproject.common.entity.HasTarget
import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.entity.TargetRef
import fr.postgresjson.entity.EntityCreatedAt
import fr.postgresjson.entity.EntityCreatedAtImp
import fr.postgresjson.entity.EntityCreatedBy
import fr.postgresjson.entity.EntityCreatedByImp
import fr.postgresjson.entity.EntityDeletedAt
import fr.postgresjson.entity.EntityDeletedAtImp
import fr.postgresjson.entity.EntityUpdatedAt
import fr.postgresjson.entity.EntityUpdatedAtImp
import org.joda.time.DateTime
import java.util.UUID

class CommentForView<T : TargetI, C : CitizenRef>(
    id: UUID = UUID.randomUUID(),
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
    EntityCreatedBy<C> by EntityCreatedByImp(createdBy),
    EntityUpdatedAt by EntityUpdatedAtImp(),
    EntityDeletedAt by EntityDeletedAtImp(),
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

open class CommentForUpdate<T : TargetI, C : CitizenRef>(
    override val id: UUID = UUID.randomUUID(),
    override val createdBy: C,
    override val target: T,
    open var content: String,
    override val parent: CommentParent<T>? = null,
    override val deletedAt: DateTime? = null
) : CommentParent<T>(id, deletedAt, target),
    CommentWithParentI<T>,
    ExtraI<T, C>,
    CommentWithTargetI<T>,
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<C>,
    EntityDeletedAt,
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

open class CommentParent<T : TargetI>(
    override val id: UUID,
    override val deletedAt: DateTime?,
    override val target: T
) : CommentRef(id),
    CommentParentI<T>

interface CommentParentI<T : TargetI> : CommentI, EntityDeletedAt, CommentWithTargetI<T>

interface CommentWithTargetI<T : TargetI> : CommentI, TargetI, HasTarget<T>

interface CommentWithParentI<T : TargetI> {
    val parent: CommentParent<T>?
}

open class CommentRef(id: UUID = UUID.randomUUID()) : CommentI, TargetRef(id)

interface CommentI : EntityI

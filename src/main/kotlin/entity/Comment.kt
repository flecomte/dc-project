package fr.dcproject.entity

import fr.postgresjson.entity.*
import org.joda.time.DateTime
import java.util.*

class CommentForView<T : TargetI, C : CitizenRef>(
    id: UUID = UUID.randomUUID(),
    override val createdBy: C,
    override val target: T,
    override var content: String,
    override val parent: CommentParent<T>? = null,
    val childrenCount: Int? = null,
    override val deletedAt: DateTime? = null
) : ExtraI<T, C>,
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

open class CommentForUpdate<T : TargetI, C: CitizenRef>(
    override val id: UUID = UUID.randomUUID(),
    override val createdBy: C,
    override val target: T,
    open var content: String,
    open val parent: CommentParent<T>? = null,
    override val deletedAt: DateTime? = null
) : CommentParent<T>(id, deletedAt, target),
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

open class CommentParent<T: TargetI>(
    override val id: UUID,
    override val deletedAt: DateTime?,
    override val target: T
) : CommentRef(id),
    CommentParentI<T>

interface CommentParentI<T: TargetI> : CommentI, EntityDeletedAt, CommentWithTargetI<T>

interface CommentWithTargetI<T : TargetI> : CommentI, TargetI, AsTarget<T>

open class CommentRef(id: UUID = UUID.randomUUID()) : CommentI, TargetRef(id)

interface CommentI : EntityI

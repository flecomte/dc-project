package fr.dcproject.entity

import fr.postgresjson.entity.immutable.*
import fr.postgresjson.entity.mutable.EntityDeletedAt
import fr.postgresjson.entity.mutable.EntityDeletedAtImp
import java.util.*

open class Comment<T : TargetI>(
    id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenBasic,
    override var target: T,
    var content: String,
    val responses: List<Comment<T>>? = null,
    var parent: Comment<T>? = null,
    val parentsIds: List<UUID>? = null,
    val childrenCount: Int? = null
) : ExtraI<T>,
    CommentRef(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<CitizenBasicI> by EntityCreatedByImp(createdBy),
    EntityUpdatedAt by EntityUpdatedAtImp(),
    EntityDeletedAt by EntityDeletedAtImp(),
    Votable by VotableImp(),
    TargetI {
    constructor(
        createdBy: CitizenBasic,
        parent: Comment<T>,
        content: String
    ) : this(
        createdBy = createdBy,
        parent = parent,
        target = parent.target,
        content = content
    )

    override val reference get() = TargetI.getReference(this)
}

open class CommentRef(id: UUID = UUID.randomUUID()) : CommentS(id)

sealed class CommentS(id: UUID) : TargetRef(id)

package fr.dcproject.entity

import fr.postgresjson.entity.immutable.EntityUpdatedAt
import fr.postgresjson.entity.immutable.EntityUpdatedAtImp
import fr.postgresjson.entity.mutable.EntityDeletedAt
import fr.postgresjson.entity.mutable.EntityDeletedAtImp
import java.util.*

open class Comment<T : TargetI>(
    id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenBasic,
    target: T,
    var content: String,
    val responses: List<Comment<T>>? = null,
    var parent: Comment<T>? = null,
    val parentsIds: List<UUID>? = null,
    val childrenCount: Int? = null
) : Extra<T>(id, createdBy, target),
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

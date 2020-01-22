package fr.dcproject.entity

import fr.postgresjson.entity.mutable.*
import java.util.*

open class Comment <T : UuidEntity> (
    id: UUID = UUID.randomUUID(),
    createdBy: Citizen,
    target: T,
    override var targetReference: String = target::class.simpleName!!.toLowerCase(),
    var content: String,
    val responses: List<Comment<T>>? = null,
    var parent: Comment<T>? = null,
    val parentsIds: List<UUID>? = null,
    val childrenCount: Int? = null
) : Extra<T>(id, createdBy, target),
    EntityUpdatedAt by EntityUpdatedAtImp(),
    EntityDeletedAt by EntityDeletedAtImp(),
    Votable by VotableImp()
{
    constructor(
        createdBy: Citizen,
        parent: Comment<T>,
        content: String
    ) : this(
        createdBy = createdBy,
        parent = parent,
        target = parent.target,
        targetReference = parent.targetReference,
        content = content
    )
}

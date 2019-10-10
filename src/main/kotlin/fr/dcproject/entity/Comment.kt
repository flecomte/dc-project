package fr.dcproject.entity

import fr.postgresjson.entity.*
import java.util.*

open class Comment <T : UuidEntity> (
    id: UUID = UUID.randomUUID(),
    createdBy: Citizen,
    target: T,
    var content: String,
    var responses: List<Comment<T>>? = null,
    var parent: Comment<T>? = null,
    var parentsIds: List<UUID>? = null,
    val childrenCount: Int? = null
) : Extra<T>(id, createdBy, target),
    EntityUpdatedAt by EntityUpdatedAtImp(),
    EntityDeletedAt by EntityDeletedAtImp(),
    Votable by VotableImp()

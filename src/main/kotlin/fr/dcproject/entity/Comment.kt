package fr.dcproject.entity

 import fr.postgresjson.entity.*
import java.util.*

open class Comment <T: UuidEntity> (
    id: UUID = UUID.randomUUID(),
    createdBy: Citizen,
    override var target: T,
    var content: String,
    var responses: List<Comment<T>>? = null,
    var parent: Comment<T>? = null,
    var parentsIds: List<UUID>? = null,
    val childrenCount: Int? = null
): Extra<T>(id, createdBy),
    EntityUpdatedAt by EntityUpdatedAtImp(),
    EntityDeletedAt by EntityDeletedAtImp()

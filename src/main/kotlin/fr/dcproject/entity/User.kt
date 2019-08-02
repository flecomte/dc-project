package fr.dcproject.entity

import fr.postgresjson.entity.*
import org.joda.time.DateTime
import java.util.*

class User(
    id: UUID? = UUID.randomUUID(),
    var username: String?,
    var blockedAt: DateTime? = null,
    var plainPassword: String?
) : UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityUpdatedAt by EntityUpdatedAtImp()

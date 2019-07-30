package fr.dcproject.entity

import fr.postgresjson.entity.*
import fr.postgresjson.entity.User
import org.joda.time.DateTime
import java.util.*

class User(
    id: UUID?,
    var username: String,
    var blockedAt: DateTime?,
    override var createdAt: DateTime?,
    override var updatedAt: DateTime?
) : UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityUpdatedAt by EntityUpdatedAtImp(),
    User<UUID?>

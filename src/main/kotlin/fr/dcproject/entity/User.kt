package fr.dcproject.entity

import fr.postgresjson.entity.*
import io.ktor.auth.Principal
import org.joda.time.DateTime
import java.util.*

class User(
    id: UUID? = UUID.randomUUID(),
    var username: String?,
    var blockedAt: DateTime? = null,
    var plainPassword: String?,
    var roles: List<Roles> = emptyList()
) : UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityUpdatedAt by EntityUpdatedAtImp(),
    Principal
{
    enum class Roles { ROLE_USER, ROLE_ADMIN }
}

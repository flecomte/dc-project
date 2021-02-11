package fr.dcproject.component.auth

import fr.dcproject.component.auth.UserI.Roles
import fr.postgresjson.entity.EntityCreatedAt
import fr.postgresjson.entity.EntityCreatedAtImp
import fr.postgresjson.entity.EntityUpdatedAt
import fr.postgresjson.entity.EntityUpdatedAtImp
import fr.postgresjson.entity.UuidEntity
import fr.postgresjson.entity.UuidEntityI
import io.ktor.auth.Principal
import org.joda.time.DateTime
import java.util.UUID

class UserForCreate(
    id: UUID = UUID.randomUUID(),
    username: String,
    override val password: String,
    blockedAt: DateTime? = null,
    roles: List<Roles> = emptyList()
) : User(id, username, blockedAt, roles),
    UserWithPasswordI

open class User(
    id: UUID = UUID.randomUUID(),
    var username: String,
    var blockedAt: DateTime? = null,
    var roles: List<Roles> = emptyList()
) : UserRef(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityUpdatedAt by EntityUpdatedAtImp()

interface UserWithPasswordI {
    val id: UUID
    val password: String
}

class UserWithPassword(
    id: UUID,
    override val password: String,
) : UserWithPasswordI,
    UserRef(id)

open class UserRef(
    id: UUID = UUID.randomUUID()
) : UserI, UuidEntity(id)

interface UserI : UuidEntityI, Principal {
    enum class Roles { ROLE_USER, ROLE_ADMIN }
}

interface UserForAuthI : UserI {
    var roles: List<Roles>
    var blockedAt: DateTime?
}

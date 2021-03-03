package fr.dcproject.component.auth.database

import fr.dcproject.common.entity.CreatedAt
import fr.dcproject.common.entity.Entity
import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.UpdatedAt
import fr.dcproject.component.auth.database.UserI.Roles
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
    override var username: String,
    var blockedAt: DateTime? = null,
    var roles: List<Roles> = emptyList()
) : UserRef(id),
    UserWithUsername,
    CreatedAt by CreatedAt.Imp(),
    UpdatedAt by UpdatedAt.Imp()

class UserCreator(
    id: UUID = UUID.randomUUID(),
    override val username: String,
) : UserRef(id), UserWithUsername

interface UserWithUsername : UserI {
    val username: String
}

interface UserWithPasswordI : UserI {
    val password: String
}

class UserWithPassword(
    id: UUID,
    override val password: String,
) : UserWithPasswordI,
    UserRef(id)

open class UserRef(
    id: UUID = UUID.randomUUID()
) : UserI, Entity(id)

interface UserI : EntityI, Principal {
    enum class Roles { ROLE_USER, ROLE_ADMIN }
}

interface UserForAuthI : UserI {
    var roles: List<Roles>
    var blockedAt: DateTime?
}

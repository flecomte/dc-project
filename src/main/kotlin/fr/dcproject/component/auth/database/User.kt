package fr.dcproject.component.auth.database

import fr.dcproject.common.entity.CreatedAt
import fr.dcproject.common.entity.Entity
import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.UpdatedAt
import fr.dcproject.component.auth.database.UserI.Roles
import io.ktor.auth.Principal
import org.joda.time.DateTime
import java.util.UUID

data class UserForCreate(
    override val id: UUID = UUID.randomUUID(),
    override val username: String,
    override val password: String,
    override val blockedAt: DateTime? = null,
    override val roles: Set<Roles> = emptySet()
) : UserForViewI,
    UserWithPasswordI,
    CreatedAt by CreatedAt.Imp(),
    UpdatedAt by UpdatedAt.Imp()

data class User(
    override val id: UUID = UUID.randomUUID(),
    override val username: String,
    override val blockedAt: DateTime? = null,
    override val roles: Set<Roles> = emptySet()
) : UserRef(id),
    UserForViewI,
    UserWithUsername,
    CreatedAt by CreatedAt.Imp(),
    UpdatedAt by UpdatedAt.Imp()

sealed interface UserForViewI :
    UserI,
    UserWithUsername,
    UserForAuthI,
    CreatedAt,
    UpdatedAt

class UserCreator(
    id: UUID = UUID.randomUUID(),
    override val username: String,
) : UserRef(id), UserWithUsername

sealed interface UserWithUsername : UserI {
    val username: String
}

sealed interface UserWithPasswordI : UserI {
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

sealed interface UserI : EntityI, Principal {
    enum class Roles { ROLE_USER, ROLE_ADMIN }
}

sealed interface UserForAuthI : UserI {
    val roles: Set<Roles>
    val blockedAt: DateTime?
}

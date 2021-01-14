package fr.dcproject.entity

import fr.dcproject.entity.UserI.Roles
import fr.postgresjson.entity.*
import io.ktor.auth.*
import org.joda.time.DateTime
import java.util.*

@Deprecated("")
class User(
    id: UUID = UUID.randomUUID(),
    username: String,
    blockedAt: DateTime? = null,
    override var plainPassword: String? = null,
    override var roles: List<Roles> = emptyList()
) : UserFull, UserBasic(id, username, blockedAt),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityUpdatedAt by EntityUpdatedAtImp()

@Deprecated("")
open class UserBasic(
    id: UUID = UUID.randomUUID(),
    override var username: String,
    override var blockedAt: DateTime? = null
) : UserBasicI, UserRef(id)

open class UserRef(
    id: UUID = UUID.randomUUID()
) : UserI, UuidEntity(id)

interface UserI : UuidEntityI, Principal {
    enum class Roles { ROLE_USER, ROLE_ADMIN }
}

@Deprecated("")
interface UserBasicI : UserI {
    var username: String
    var blockedAt: DateTime?
}

@Deprecated("")
interface UserFull : UserBasicI, EntityCreatedAt, EntityUpdatedAt {
    var plainPassword: String?
    var roles: List<Roles>
}

interface UserForAuthI : UserI {
    var roles: List<Roles>
    var blockedAt: DateTime?
}

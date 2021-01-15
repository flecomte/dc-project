package fr.dcproject.component.citizen

import fr.dcproject.component.citizen.CitizenI.Name
import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.auth.UserRef
import fr.dcproject.entity.WorkgroupSimple
import fr.postgresjson.entity.*
import org.joda.time.DateTime
import java.util.*

class Citizen(
    override val id: UUID = UUID.randomUUID(),
    override val name: Name,
    override val email: String,
    override val birthday: DateTime,
    override val voteAnonymous: Boolean = true,
    override val followAnonymous: Boolean = true,
    override val user: User,
    deletedAt: DateTime? = null
) : CitizenFull,
    CitizenBasicI,
    CitizenRef(id),
    CitizenCartI,
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityDeletedAt by EntityDeletedAtImp(deletedAt) {
    var workgroups: List<WorkgroupAndRoles> = emptyList()

    class WorkgroupAndRoles(
        val roles: List<String>,
        val workgroup: WorkgroupSimple<CitizenRef>
    )
}

@Deprecated("")
data class CitizenBasic(
    override var id: UUID = UUID.randomUUID(),
    override var name: Name,
    override var email: String,
    override var birthday: DateTime,
    override var voteAnonymous: Boolean = true,
    override var followAnonymous: Boolean = true,
    override val user: User,
    override val deletedAt: DateTime? = null
) : CitizenBasicI,
    CitizenRefWithUser(id, user),
    EntityDeletedAt by EntityDeletedAtImp(deletedAt)

@Deprecated("")
open class CitizenSimple(
    id: UUID = UUID.randomUUID(),
    var name: Name,
    user: UserRef
) : CitizenRefWithUser(id, user)

class CitizenCart(
    id: UUID = UUID.randomUUID(),
    override val name: Name,
    override val user: UserRef
) : CitizenRef(id),
    CitizenCartI

interface CitizenCartI : CitizenI, CitizenWithUserI {
    val name: Name
}

open class CitizenRefWithUser(
    id: UUID = UUID.randomUUID(),
    override val user: UserRef
) : CitizenWithUserI,
    CitizenRef(id)

open class CitizenRef(
    id: UUID = UUID.randomUUID()
) : UuidEntity(id),
    CitizenI

interface CitizenI : UuidEntityI {
    data class Name(
        override val firstName: String,
        override val lastName: String,
        override val civility: String? = null
    ) : NameI

    interface NameI {
        val firstName: String
        val lastName: String
        val civility: String?
        fun getFullName(): String = "${civility ?: ""} $firstName $lastName".trim()
    }
}

@Deprecated("")
interface CitizenBasicI : CitizenWithUserI, CitizenWithEmail, EntityDeletedAt {
    val name: Name
    val birthday: DateTime
    val voteAnonymous: Boolean
    val followAnonymous: Boolean
}

@Deprecated("")
interface CitizenFull : CitizenBasicI {
    override val user: User
}

interface CitizenWithUserI : CitizenI {
    val user: UserI
}

interface CitizenWithEmail : CitizenI {
    val email: String
}

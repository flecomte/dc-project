package fr.dcproject.component.citizen

import fr.dcproject.common.entity.CreatedAt
import fr.dcproject.common.entity.DeletedAt
import fr.dcproject.common.entity.Entity
import fr.dcproject.common.entity.EntityI
import fr.dcproject.component.auth.User
import fr.dcproject.component.auth.UserForCreate
import fr.dcproject.component.auth.UserI
import fr.dcproject.component.auth.UserRef
import fr.dcproject.component.citizen.CitizenI.Name
import fr.dcproject.component.workgroup.WorkgroupSimple
import fr.postgresjson.entity.Serializable
import org.joda.time.DateTime
import java.util.UUID

class CitizenForCreate(
    val name: Name,
    val email: String,
    val birthday: DateTime,
    val voteAnonymous: Boolean = true,
    val followAnonymous: Boolean = true,
    override val user: UserForCreate,
    id: UUID = UUID.randomUUID(),
) : CitizenI,
    CitizenRefWithUser(id, user),
    CreatedAt by CreatedAt.Imp()

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
    CitizenWithUserI,
    CitizenRef(id),
    CitizenCartI,
    CreatedAt by CreatedAt.Imp(),
    DeletedAt by DeletedAt.Imp(deletedAt) {
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
    DeletedAt by DeletedAt.Imp(deletedAt)

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
) : Entity(id),
    CitizenI

interface CitizenI : EntityI {
    data class Name(
        override val firstName: String,
        override val lastName: String,
        override val civility: String? = null
    ) : NameI

    interface NameI : Serializable {
        val firstName: String
        val lastName: String
        val civility: String?
        fun getFullName(): String = "${civility ?: ""} $firstName $lastName".trim()
    }
}

@Deprecated("")
interface CitizenBasicI : CitizenWithUserI, CitizenWithEmail, DeletedAt {
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

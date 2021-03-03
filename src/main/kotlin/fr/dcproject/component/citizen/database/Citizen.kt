package fr.dcproject.component.citizen.database

import fr.dcproject.common.entity.CreatedAt
import fr.dcproject.common.entity.DeletedAt
import fr.dcproject.common.entity.Entity
import fr.dcproject.common.entity.EntityI
import fr.dcproject.component.auth.database.User
import fr.dcproject.component.auth.database.UserCreator
import fr.dcproject.component.auth.database.UserForCreate
import fr.dcproject.component.auth.database.UserI
import fr.dcproject.component.auth.database.UserRef
import fr.dcproject.component.auth.database.UserWithUsername
import fr.dcproject.component.citizen.database.CitizenI.Name
import fr.dcproject.component.workgroup.database.WorkgroupRef
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
    val birthday: DateTime,
    override val voteAnonymous: Boolean = true,
    override val followAnonymous: Boolean = true,
    override val user: User,
    deletedAt: DateTime? = null
) : CitizenWithEmail,
    CitizenCreatorI,
    CitizenWithUserI,
    CitizenRef(id),
    CitizenCartI,
    CreatedAt by CreatedAt.Imp(),
    DeletedAt by DeletedAt.Imp(deletedAt) {
    var workgroups: List<WorkgroupAndRoles> = emptyList()

    class WorkgroupAndRoles(
        val roles: List<String>,
        val workgroup: WorkgroupRef
    )
}

data class CitizenCreator(
    override var id: UUID = UUID.randomUUID(),
    override var name: Name,
    override var email: String,
    override var voteAnonymous: Boolean = true,
    override var followAnonymous: Boolean = true,
    override val user: UserCreator,
    override val deletedAt: DateTime? = null
) : CitizenCreatorI,
    CitizenRefWithUser(id, user),
    DeletedAt by DeletedAt.Imp(deletedAt)

interface CitizenCreatorI : CitizenWithUserI, CitizenWithEmail, CitizenCartI, DeletedAt {
    override val id: UUID
    override val name: Name
    override val email: String
    val voteAnonymous: Boolean
    val followAnonymous: Boolean
    override val user: UserWithUsername
    override val deletedAt: DateTime?
}

class CitizenCart(
    id: UUID = UUID.randomUUID(),
    override val name: Name,
    override val user: UserRef,
    override val deletedAt: DateTime? = null,
) : CitizenRef(id),
    CitizenCartI,
    DeletedAt by DeletedAt.Imp(deletedAt)

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

interface CitizenWithUserI : CitizenI {
    val user: UserI
}

interface CitizenWithEmail : CitizenI {
    val email: String
}

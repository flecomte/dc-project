package fr.dcproject.entity

import fr.dcproject.entity.CitizenI.Name
import fr.postgresjson.entity.immutable.EntityCreatedAt
import fr.postgresjson.entity.immutable.EntityCreatedAtImp
import fr.postgresjson.entity.immutable.UuidEntity
import fr.postgresjson.entity.immutable.UuidEntityI
import fr.postgresjson.entity.mutable.EntityDeletedAt
import fr.postgresjson.entity.mutable.EntityDeletedAtImp
import org.joda.time.DateTime
import java.util.*

class Citizen(
    id: UUID = UUID.randomUUID(),
    name: Name,
    email: String,
    birthday: DateTime,
    voteAnonymous: Boolean = true,
    followAnonymous: Boolean = true,
    override val user: User
) : CitizenFull,
    CitizenBasic(id, name, email, birthday, voteAnonymous, followAnonymous, user),
    EntityCreatedAt by EntityCreatedAtImp() {
    var workgroups: List<WorkgroupSimple<CitizenRef>> = emptyList()
}

open class CitizenBasic(
    id: UUID = UUID.randomUUID(),
    name: Name,
    override var email: String,
    override var birthday: DateTime,
    override var voteAnonymous: Boolean = true,
    override var followAnonymous: Boolean = true,
    override val user: User
) : CitizenBasicI,
    CitizenSimple(id, name, user)

open class CitizenSimple(
    id: UUID = UUID.randomUUID(),
    var name: Name,
    user: UserRef
) : CitizenRefWithUser(id, user)

open class CitizenRefWithUser(
    id: UUID = UUID.randomUUID(),
    override val user: UserRef
) : CitizenWithUserI,
    CitizenRef(id),
    EntityDeletedAt by EntityDeletedAtImp()

open class CitizenRef(
    id: UUID = UUID.randomUUID()
) : UuidEntity(id),
    CitizenI

interface CitizenI : UuidEntityI {
    data class Name(
        var firstName: String,
        var lastName: String,
        var civility: String? = null
    ) {
        fun getFullName(): String = "${civility ?: ""} $firstName $lastName".trim()
    }
}

interface CitizenBasicI : CitizenWithUserI, EntityDeletedAt {
    var name: Name
    var email: String
    var birthday: DateTime
    var voteAnonymous: Boolean
    var followAnonymous: Boolean
}

interface CitizenFull : CitizenBasicI {
    override val user: User
}

interface CitizenWithUserI : CitizenI {
    val user: UserI
}

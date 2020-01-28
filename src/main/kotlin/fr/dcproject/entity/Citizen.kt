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
    EntityCreatedAt by EntityCreatedAtImp()

open class CitizenBasic(
    id: UUID = UUID.randomUUID(),
    name: Name,
    override var email: String,
    override var birthday: DateTime,
    override var voteAnonymous: Boolean = true,
    override var followAnonymous: Boolean = true,
    user: UserRef
) : CitizenBasicI,
    CitizenSimple(id, name, user)

open class CitizenSimple(
    id: UUID = UUID.randomUUID(),
    var name: Name,
    user: UserRef
) : CitizenRef(id, user)

open class CitizenRef(
    id: UUID = UUID.randomUUID(),
    open val user: UserRef
) : UuidEntity(id),
    CitizenI,
    EntityDeletedAt by EntityDeletedAtImp()

interface CitizenI : UuidEntityI {
    data class Name(
        var firstName: String,
        var lastName: String,
        var civility: String? = null
    )
}

interface CitizenBasicI : CitizenI, EntityDeletedAt {
    var name: Name
    var email: String
    var birthday: DateTime
    var voteAnonymous: Boolean
    var followAnonymous: Boolean
    val user: UserI
}

interface CitizenFull : CitizenBasicI {
    override val user: User
}

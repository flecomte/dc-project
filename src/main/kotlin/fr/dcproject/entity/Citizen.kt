package fr.dcproject.entity

import fr.postgresjson.entity.*
import org.joda.time.DateTime
import java.util.*

class Citizen(
    id: UUID = UUID.randomUUID(),
    var name: Name?,
    var birthday: DateTime?,
    var userId: UUID? = null,
    var voteAnonymous: Boolean = true,
    var followAnonymous: Boolean = true,
    var user: User?
) : UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityDeletedAt by EntityDeletedAtImp() {
    data class Name(
        var firstName: String?,
        var lastName: String?,
        var civility: String? = null
    )
}
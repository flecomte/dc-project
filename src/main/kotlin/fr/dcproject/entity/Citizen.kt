package fr.dcproject.entity

import fr.postgresjson.entity.EntityCreatedAt
import fr.postgresjson.entity.EntityCreatedAtImp
import fr.postgresjson.entity.UuidEntity
import org.joda.time.DateTime
import java.util.*


class Citizen(
    id: UUID = UUID.randomUUID(),
    var name: Name?,
    var birthday: DateTime?,
    var userId: String? = null,
    var voteAnnonymous: Boolean? = null,
    var followAnnonymous: Boolean? = null,
    var user: User?
) : UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp() {
    data class Name(
        var firstName: String?,
        var lastName: String?,
        var civility: String? = null
    )
}
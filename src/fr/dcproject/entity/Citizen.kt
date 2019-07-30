package fr.dcproject.entity

import fr.postgresjson.entity.EntityCreatedAt
import fr.postgresjson.entity.EntityCreatedAtImp
import fr.postgresjson.entity.UuidEntity
import java.util.*


class Citizen(
    id: UUID,
    var name: Name,
    var birthday: String,
    var userId: String,
    var voteAnnonymous: Boolean,
    var followAnnonymous: Boolean,
    var user: User
) : UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp() {
    data class Name(
        var civility: String,
        var lastName: String,
        var firstName: String
    )
}
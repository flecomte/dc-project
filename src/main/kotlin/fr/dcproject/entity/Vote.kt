package fr.dcproject.entity

import fr.postgresjson.entity.mutable.EntityUpdatedAt
import fr.postgresjson.entity.mutable.EntityUpdatedAtImp
import fr.postgresjson.entity.mutable.UuidEntity
import java.util.*

open class Vote <T : UuidEntity> (
    id: UUID = UUID.randomUUID(),
    createdBy: Citizen,
    target: T,
    var note: Int,
    var anonymous: Boolean = true
) : Extra<T>(id, createdBy, target),
    EntityUpdatedAt by EntityUpdatedAtImp() {
    init {
        if (note > 1 && note < -1) {
            error("note must be 1, 0 or -1")
        }
    }
}

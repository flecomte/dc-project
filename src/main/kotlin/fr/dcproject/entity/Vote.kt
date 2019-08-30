package fr.dcproject.entity

import fr.postgresjson.entity.EntityUpdatedAt
import fr.postgresjson.entity.EntityUpdatedAtImp
import fr.postgresjson.entity.UuidEntity
import java.util.*

open class Vote <T: UuidEntity> (
    id: UUID = UUID.randomUUID(),
    createdBy: Citizen,
    override var target: T,
    var note: Int,
    var anonymous: Boolean = true
): Extra<T>(id, createdBy),
    EntityUpdatedAt by EntityUpdatedAtImp() {
    init {
        if (note > 1 && note < -1) {
            error("note must be 1, 0 or -1")
        }
    }
}

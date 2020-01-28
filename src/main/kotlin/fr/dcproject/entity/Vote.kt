package fr.dcproject.entity

import fr.postgresjson.entity.immutable.EntityUpdatedAt
import fr.postgresjson.entity.immutable.EntityUpdatedAtImp
import java.util.*

open class Vote <T : TargetI> (
    id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenBasic,
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

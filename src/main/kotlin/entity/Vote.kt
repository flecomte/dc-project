package fr.dcproject.entity

import fr.postgresjson.entity.immutable.*
import java.util.*

open class Vote<T : TargetI>(
    id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenBasic,
    override var target: T,
    var note: Int,
    var anonymous: Boolean = true
) : ExtraI<T, CitizenBasicI>,
    UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<CitizenBasicI> by EntityCreatedByImp(createdBy),
    EntityUpdatedAt by EntityUpdatedAtImp() {
    init {
        if (note > 1 && note < -1) {
            error("note must be 1, 0 or -1")
        }
    }
}

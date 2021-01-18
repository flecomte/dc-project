package fr.dcproject.entity

import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenBasicI
import fr.dcproject.component.citizen.CitizenI
import fr.postgresjson.entity.*
import java.util.*
@Deprecated("")
class Vote<T : TargetI>(
    id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenBasic,
    override val target: T,
    var note: Int,
    var anonymous: Boolean = true
) : ExtraI<T, CitizenBasicI>,
    VoteRef(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<CitizenBasicI> by EntityCreatedByImp(createdBy),
    EntityUpdatedAt by EntityUpdatedAtImp() {
    init {
        if (note > 1 && note < -1) {
            error("note must be 1, 0 or -1")
        }
    }
}

class VoteForUpdate<T : TargetI, C : CitizenI>(
    override val id: UUID = UUID.randomUUID(),
    override val note: Int,
    override val target: T,
    override val createdBy: C
) : VoteRef(id),
    VoteForUpdateI<T, C>,
    EntityCreatedBy<C> by EntityCreatedByImp<C>(createdBy)

interface VoteForUpdateI<T : TargetI, C : CitizenI> : VoteI, HasTarget<T>, EntityCreatedBy<C> {
    override val id: UUID
    val note: Int
    override val target: T
    override val createdBy: C
}

open class VoteRef(
    override val id: UUID
) : VoteI

interface VoteI : UuidEntityI

package fr.dcproject.component.vote.entity

import fr.dcproject.common.entity.CreatedAt
import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.ExtraI
import fr.dcproject.common.entity.HasTarget
import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.entity.UpdatedAt
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenBasicI
import fr.dcproject.component.citizen.CitizenI
import java.util.UUID

@Deprecated("")
class Vote<T : TargetI>(
    id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenBasic,
    override val target: T,
    var note: Int,
    var anonymous: Boolean = true
) : ExtraI<T, CitizenBasicI>,
    VoteRef(id),
    CreatedAt by CreatedAt.Imp(),
    CreatedBy<CitizenBasicI> by CreatedBy.Imp(createdBy),
    UpdatedAt by UpdatedAt.Imp() {
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
    CreatedBy<C> by CreatedBy.Imp<C>(createdBy)

interface VoteForUpdateI<T : TargetI, C : CitizenI> : VoteI, HasTarget<T>, CreatedBy<C> {
    override val id: UUID
    val note: Int
    override val target: T
    override val createdBy: C
}

open class VoteRef(
    override val id: UUID
) : VoteI

interface VoteI : EntityI

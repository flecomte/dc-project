package fr.dcproject.component.vote.database

import fr.dcproject.common.entity.CreatedAt
import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.ExtraI
import fr.dcproject.common.entity.HasTarget
import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.entity.UpdatedAt
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenCreatorI
import fr.dcproject.component.citizen.database.CitizenI
import java.util.UUID

data class VoteForView<T : TargetI>(
    override val id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenCreator,
    override val target: T,
    val note: Int,
    val anonymous: Boolean = true
) : ExtraI<T, CitizenCreatorI>,
    VoteRef(id),
    CreatedAt by CreatedAt.Imp(),
    CreatedBy<CitizenCreatorI> by CreatedBy.Imp(createdBy),
    UpdatedAt by UpdatedAt.Imp() {
    init {
        if (note > 1 && note < -1) {
            error("note must be 1, 0 or -1")
        }
    }
}

data class VoteForUpdate<T : TargetI, C : CitizenI>(
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

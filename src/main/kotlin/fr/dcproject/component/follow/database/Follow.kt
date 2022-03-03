package fr.dcproject.component.follow.database

import fr.dcproject.common.entity.Created
import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.ExtraI
import fr.dcproject.common.entity.HasTarget
import fr.dcproject.common.entity.TargetI
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenI
import java.util.UUID

data class FollowForView<T : TargetI>(
    override val id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenCreator,
    override var target: T
) : ExtraI<T, CitizenI>,
    FollowRef(id),
    Created<CitizenI> by Created.Imp(createdBy)

data class FollowForUpdate<T : TargetI, C : CitizenI>(
    override val id: UUID = UUID.randomUUID(),
    override val target: T,
    override val createdBy: C
) : FollowRef(id),
    HasTarget<T>,
    CreatedBy<C> by CreatedBy.Imp<C>(createdBy)

open class FollowRef(
    override val id: UUID
) : FollowI

sealed interface FollowI : EntityI

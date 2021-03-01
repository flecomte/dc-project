package fr.dcproject.component.follow

import fr.dcproject.common.entity.Created
import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.ExtraI
import fr.dcproject.common.entity.HasTarget
import fr.dcproject.common.entity.TargetI
import fr.dcproject.component.citizen.CitizenCreator
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenRef
import java.util.UUID

open class FollowForView<T : TargetI>(
    id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenCreator,
    override var target: T
) : ExtraI<T, CitizenRef>,
    FollowRef(id),
    Created<CitizenRef> by Created.Imp(createdBy)

class FollowForUpdate<T : TargetI, C : CitizenI>(
    id: UUID = UUID.randomUUID(),
    override val target: T,
    override val createdBy: C
) : FollowRef(id),
    HasTarget<T>,
    CreatedBy<C> by CreatedBy.Imp<C>(createdBy)

open class FollowRef(
    override val id: UUID
) : FollowI

interface FollowI : EntityI

package fr.dcproject.component.follow

import fr.dcproject.common.entity.Created
import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.ExtraI
import fr.dcproject.common.entity.HasTarget
import fr.dcproject.common.entity.TargetI
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenBasicI
import fr.dcproject.component.citizen.CitizenI
import java.util.UUID

@Deprecated("")
class Follow<T : TargetI>(
    id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenBasic,
    override var target: T
) : ExtraI<T, CitizenBasicI>,
    FollowSimple<T, CitizenBasicI>(id, createdBy, target)

@Deprecated("")
open class FollowSimple<T : TargetI, C : CitizenI>(
    id: UUID = UUID.randomUUID(),
    override val createdBy: C,
    override var target: T
) : ExtraI<T, C>,
    FollowRef(id),
    Created<C> by Created.Imp(createdBy)

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

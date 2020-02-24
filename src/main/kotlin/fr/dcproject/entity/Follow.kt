package fr.dcproject.entity

import fr.postgresjson.entity.immutable.*
import java.util.*

class Follow<T : TargetI>(
    id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenBasic,
    override var target: T
) : ExtraI<T, CitizenBasicI>,
    FollowSimple<T, CitizenBasicI>(id, createdBy, target)

open class FollowSimple<T : TargetI, C: CitizenI>(
    id: UUID = UUID.randomUUID(),
    override val createdBy: C,
    override var target: T
) : ExtraI<T, C>,
    UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<C> by EntityCreatedByImp(createdBy)

package fr.dcproject.entity

import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenBasicI
import fr.dcproject.component.citizen.CitizenI
import fr.postgresjson.entity.EntityCreatedAt
import fr.postgresjson.entity.EntityCreatedAtImp
import fr.postgresjson.entity.EntityCreatedBy
import fr.postgresjson.entity.EntityCreatedByImp
import fr.postgresjson.entity.UuidEntityI
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
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<C> by EntityCreatedByImp(createdBy)

class FollowForUpdate<T : TargetI, C : CitizenI>(
    id: UUID = UUID.randomUUID(),
    override val target: T,
    override val createdBy: C
) : FollowRef(id),
    HasTarget<T>,
    EntityCreatedBy<C> by EntityCreatedByImp<C>(createdBy)

open class FollowRef(
    override val id: UUID
) : FollowI

interface FollowI : UuidEntityI

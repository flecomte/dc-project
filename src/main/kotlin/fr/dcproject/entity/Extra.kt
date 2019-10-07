package fr.dcproject.entity

import fr.postgresjson.entity.*
import java.util.*

interface ExtraI <T: EntityI>:
    EntityI,
    EntityCreatedAt,
    EntityCreatedBy<Citizen>{
    var target: T
    var targetReference: String
}

abstract class Extra<T: UuidEntity>(
    id: UUID? = UUID.randomUUID(),
    createdBy: Citizen,
    override var target: T,
    override var targetReference: String = target::class.simpleName!!.toLowerCase()
):
    ExtraI<T>,
    UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<Citizen> by EntityCreatedByImp(createdBy)
package fr.dcproject.entity

import fr.postgresjson.entity.*
import java.util.*

interface ExtraI <T: EntityI>:
    EntityI,
    EntityCreatedAt,
    EntityCreatedBy<Citizen>{
    var target: T
}

abstract class Extra<T: UuidEntity>(
    id: UUID? = UUID.randomUUID(),
    createdBy: Citizen
):
    ExtraI<T>,
    UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<Citizen> by EntityCreatedByImp(createdBy)
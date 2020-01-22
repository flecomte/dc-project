package fr.dcproject.entity
import fr.postgresjson.entity.mutable.UuidEntity
import java.util.*

class Follow <T : UuidEntity> (
    id: UUID = UUID.randomUUID(),
    createdBy: Citizen,
    target: T
) : Extra<T>(id, createdBy, target)

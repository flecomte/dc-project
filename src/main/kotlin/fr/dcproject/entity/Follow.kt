package fr.dcproject.entity
import fr.postgresjson.entity.UuidEntity
import java.util.*

class Follow <T: UuidEntity> (
    id: UUID = UUID.randomUUID(),
    createdBy: Citizen,
    override var target: T
): Extra<T>(id, createdBy)

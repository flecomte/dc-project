package fr.dcproject.entity
import fr.postgresjson.entity.UuidEntity
import java.util.*

class Follow <T: UuidEntity> (
    id: UUID = UUID.randomUUID(),
    citizen: Citizen,
    override var target: T
): Extra<T>(id, citizen)

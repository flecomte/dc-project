package fr.dcproject.entity
import java.util.*

class Follow <T : TargetI> (
    id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenBasic,
    target: T
) : Extra<T>(id, createdBy, target)

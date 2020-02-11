package fr.dcproject.entity

import fr.postgresjson.entity.immutable.EntityCreatedAt
import fr.postgresjson.entity.immutable.EntityCreatedAtImp
import fr.postgresjson.entity.immutable.UuidEntity
import fr.postgresjson.entity.mutable.EntityDeletedAt
import fr.postgresjson.entity.mutable.EntityDeletedAtImp
import java.util.*

class OpinionChoice(
    id: UUID,
    val name: String,
    val target: List<String>
) : UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityDeletedAt by EntityDeletedAtImp()
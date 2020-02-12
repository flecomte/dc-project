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
) : OpinionChoiceRef(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityDeletedAt by EntityDeletedAtImp()

open class OpinionChoiceRef(
    id: UUID
) : UuidEntity(id)
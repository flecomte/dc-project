package fr.dcproject.component.opinion.entity

import fr.postgresjson.entity.EntityCreatedAt
import fr.postgresjson.entity.EntityCreatedAtImp
import fr.postgresjson.entity.EntityDeletedAt
import fr.postgresjson.entity.EntityDeletedAtImp
import fr.postgresjson.entity.UuidEntity
import java.util.UUID

class OpinionChoice(
    id: UUID? = null,
    val name: String,
    val target: List<String>?
) : OpinionChoiceRef(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityDeletedAt by EntityDeletedAtImp()

open class OpinionChoiceRef(
    id: UUID?
) : UuidEntity(id ?: UUID.randomUUID())

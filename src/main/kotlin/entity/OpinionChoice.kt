package fr.dcproject.entity

import fr.postgresjson.entity.*
import java.util.*

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

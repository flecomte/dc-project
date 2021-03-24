package fr.dcproject.component.opinion.database

import fr.dcproject.common.entity.CreatedAt
import fr.dcproject.common.entity.DeletedAt
import fr.dcproject.common.entity.Entity
import fr.dcproject.common.entity.EntityI
import java.util.UUID

class OpinionChoice(
    id: UUID? = null,
    val name: String,
    val target: List<String>?
) : OpinionChoiceRef(id),
    CreatedAt by CreatedAt.Imp(),
    DeletedAt by DeletedAt.Imp()

open class OpinionChoiceRef(
    id: UUID?
) : OpinionChoiceI,
    Entity(id ?: UUID.randomUUID())

interface OpinionChoiceI : EntityI

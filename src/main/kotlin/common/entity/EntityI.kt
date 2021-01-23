package fr.dcproject.common.entity

import fr.postgresjson.entity.EntityI
import java.util.UUID

interface EntityI : EntityI {
    val id: UUID
}

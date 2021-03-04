package fr.dcproject.common.dto

import fr.dcproject.component.citizen.database.CitizenI
import java.util.UUID
import fr.dcproject.common.entity.CreatedBy as EntityCreatedBy

interface CreatedBy {
    val createdBy: UUID

    class Imp(parent: EntityCreatedBy<CitizenI>) : CreatedBy {
        override val createdBy: UUID = parent.createdBy.id
    }
}

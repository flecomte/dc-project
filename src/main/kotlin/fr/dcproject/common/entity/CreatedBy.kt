package fr.dcproject.common.entity

import fr.dcproject.component.citizen.CitizenI
import fr.postgresjson.entity.EntityCreatedBy
import fr.postgresjson.entity.EntityI

/**
 * TODO remove EntityCreatedBy<EntityI>
 */
interface CreatedBy<T : CitizenI> : EntityCreatedBy<EntityI> {
    override val createdBy: T
}

class CreatedByImp<T : CitizenI>(override val createdBy: T) : CreatedBy<T>

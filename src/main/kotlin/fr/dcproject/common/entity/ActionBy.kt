package fr.dcproject.common.entity

import fr.dcproject.component.citizen.database.CitizenI

interface CreatedBy<T : CitizenI> {
    val createdBy: T

    class Imp<T : CitizenI>(override val createdBy: T) : CreatedBy<T>
}

interface UpdatedBy<T : CitizenI> {
    val updatedBy: T

    class Imp<T : CitizenI>(override val updatedBy: T) : UpdatedBy<T>
}

interface DeletedBy<T : CitizenI> {
    val deletedBy: T?

    fun isDeleted(): Boolean {
        return deletedBy?.let { true } ?: false
    }

    class Imp<T : CitizenI>(override val deletedBy: T?) : DeletedBy<T>
}

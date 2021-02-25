package fr.dcproject.common.entity

import org.joda.time.DateTime

/* Interface */
interface CreatedAt {
    val createdAt: DateTime
    class Imp(
        override val createdAt: DateTime = DateTime.now()
    ) : CreatedAt
}
interface UpdatedAt {
    val updatedAt: DateTime
    class Imp(
        override val updatedAt: DateTime = DateTime.now()
    ) : UpdatedAt
}

interface DeletedAt {
    val deletedAt: DateTime?
    fun isDeleted(): Boolean {
        return deletedAt?.let {
            it < DateTime.now()
        } ?: false
    }

    class Imp(
        override val deletedAt: DateTime? = null
    ) : DeletedAt
}

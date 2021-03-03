package fr.dcproject.common.entity

import fr.dcproject.component.citizen.database.CitizenI

interface Created<C : CitizenI> : CreatedAt, CreatedBy<C> {
    class Imp<C : CitizenI>(createdBy: C) :
        Created<C>,
        CreatedBy<C> by CreatedBy.Imp(createdBy),
        CreatedAt by CreatedAt.Imp()
}

interface Updated<C : CitizenI> : UpdatedAt, UpdatedBy<C> {
    class Imp<C : CitizenI>(updatedAt: C) :
        Updated<C>,
        UpdatedBy<C> by UpdatedBy.Imp(updatedAt),
        UpdatedAt by UpdatedAt.Imp()
}

interface Deleted<C : CitizenI> : DeletedAt, DeletedBy<C> {
    override fun isDeleted(): Boolean = (this as DeletedAt).isDeleted()

    class Imp<C : CitizenI>(deletedAt: C) :
        Deleted<C>,
        DeletedBy<C> by DeletedBy.Imp(deletedAt),
        DeletedAt by DeletedAt.Imp() {
        override fun isDeleted(): Boolean = (this as Deleted<C>).isDeleted()
    }
}

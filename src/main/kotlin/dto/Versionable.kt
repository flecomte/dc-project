package fr.dcproject.dto

import fr.postgresjson.entity.EntityVersioning
import java.util.*

interface Versionable {
    val versionId: UUID
    val versionNumber: Int

    class Imp(parent: EntityVersioning<UUID, Int>) : Versionable {
        override val versionNumber: Int = parent.versionNumber
        override val versionId: UUID = parent.versionId
    }
}


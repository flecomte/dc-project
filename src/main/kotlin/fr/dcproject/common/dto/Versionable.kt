package fr.dcproject.common.dto

import fr.dcproject.common.entity.Versionable as VersionableEntity
import java.util.UUID

interface Versionable {
    val versionId: UUID
    val versionNumber: Int

    class Imp(parent: VersionableEntity) : Versionable {
        override val versionNumber: Int = parent.versionNumber
        override val versionId: UUID = parent.versionId
    }
}

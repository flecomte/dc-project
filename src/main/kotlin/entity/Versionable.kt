package fr.dcproject.entity

import fr.postgresjson.entity.EntityVersioning
import java.util.UUID

interface VersionableRef {
    val versionId: UUID
}

class VersionableRefImp(
    override val versionId: UUID
) : VersionableRef

interface Versionable : VersionableRef, EntityVersioning<UUID, Int> {
    override val versionId: UUID
    override val versionNumber: Int
}

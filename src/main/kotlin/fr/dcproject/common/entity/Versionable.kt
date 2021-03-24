package fr.dcproject.common.entity

import java.util.UUID

interface VersionableId {
    val versionId: UUID

    class Imp(
        versionId: UUID? = null,
    ) : VersionableId {
        override val versionId: UUID = versionId ?: UUID.randomUUID()
    }
}

interface Versionable : VersionableId {
    override val versionId: UUID
    val versionNumber: Int

    class Imp(
        override val versionNumber: Int,
        versionId: UUID? = null,
    ) : Versionable {
        override val versionId: UUID = versionId ?: UUID.randomUUID()
    }
}

package fr.dcproject.component.article

import fr.dcproject.component.citizen.*
import fr.dcproject.entity.*
import fr.postgresjson.entity.*
import org.joda.time.DateTime
import java.util.*

data class ArticleForView(
    override val id: UUID = UUID.randomUUID(),
    override val title: String,
    val anonymous: Boolean = true,
    val content: String,
    val description: String,
    val tags: List<String> = emptyList(),
    override val createdBy: CitizenRef,
    override val versionNumber: Int = 0,
    override val versionId: UUID = UUID.randomUUID(),
    val workgroup: WorkgroupSimple<CitizenRef>? = null,
    override val opinions: Opinions = emptyMap(),
    override val draft: Boolean = false,
    override val deletedAt: DateTime? = null
) : ArticleRef(id),
    ArticleAuthI<CitizenRef>,
    ArticleWithTitleI,
    EntityVersioning<UUID, Int>,
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityDeletedAt by EntityDeletedAtImp(deletedAt),
    ArticleRefVersioningI,
    Opinionable,
    Votable by VotableImp() {
    val lastVersion: Boolean = false
}

interface ArticleForUpdateI<C : CitizenRef> : ArticleI, ArticleWithTitleI, VersionableRef, TargetI, CreatedBy<C> {
    val anonymous: Boolean
    val content: String
    val description: String
    val draft: Boolean
    val workgroup: WorkgroupRef?
}

class ArticleForUpdate(
    id: UUID? = null,
    override val title: String,
    override val anonymous: Boolean = true,
    override val content: String,
    override val description: String,
    tags: List<String> = emptyList(),
    override val draft: Boolean = false,
    override val createdBy: CitizenRef,
    override val workgroup: WorkgroupRef? = null,
    versionId: UUID? = null,
    override val deletedAt: DateTime? = null
) : ArticleForUpdateI<CitizenRef>,
    ArticleAuthI<CitizenRef>,
    ArticleRefVersioningI by ArticleRefVersioningImmutable(id, versionId = versionId ?: UUID.randomUUID()) {
    val tags: List<String> = tags.distinct()
    val isNew = versionId == null
}

@Deprecated("")
open class ArticleSimple(
    id: UUID = UUID.randomUUID(),
    var title: String,
    override val createdBy: CitizenBasic,
    override var draft: Boolean = false,
    var workgroup: WorkgroupSimple<CitizenRef>? = null
) : ArticleAuthI<CitizenBasicI>,
    ArticleRefVersioning(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    CreatedBy<CitizenBasicI> by CreatedByImp(createdBy),
    EntityDeletedAt by EntityDeletedAtImp(),
    Votable by VotableImp(),
    Opinionable by OpinionableImp()

class ArticleForListing(
    id: UUID? = null,
    override val title: String,
    override val createdBy: CitizenCart,
    override val workgroup: WorkgroupCart?,
    override val deletedAt: DateTime?,
    override val draft: Boolean
) : ArticleForListingI,
    ArticleRef(id),
    ArticleAuthI<CitizenCartI>,
    Votable by VotableImp(),
    CreatedBy<CitizenCartI>

interface ArticleForListingI : ArticleWithTitleI, CreatedBy<CitizenCartI> {
    val workgroup: WorkgroupCartI?
}

@Deprecated("", ReplaceWith("ArticleRefVersioningImmutable"))
open class ArticleRefVersioning(
    id: UUID = UUID.randomUUID(),
    override var versionNumber: Int = 0,
    versionId: UUID = UUID.randomUUID()
) : ArticleRefVersioningI,
    ArticleRef(id),
    EntityVersioning<UUID, Int> by UuidEntityVersioning(versionNumber, versionId)

open class ArticleRefVersioningImmutable(
    id: UUID? = null,
    versionId: UUID = UUID.randomUUID()
) : ArticleRefVersioningI,
    ArticleRef(id),
    VersionableRef by VersionableRefImp(versionId)

interface ArticleRefVersioningI : ArticleI, VersionableRef

open class ArticleRef(
    id: UUID? = null
) : ArticleI, TargetRef(id)

interface ArticleI : UuidEntityI, TargetI

interface ArticleWithTitleI : ArticleI {
    val title: String
}

interface ArticleAuthI<U : CitizenI> :
    ArticleI,
    CreatedBy<U>,
    EntityDeletedAt {
    val draft: Boolean
}
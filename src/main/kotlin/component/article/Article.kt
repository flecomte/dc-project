package fr.dcproject.component.article

import fr.dcproject.component.citizen.CitizenCart
import fr.dcproject.component.citizen.CitizenCartI
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.opinion.entity.Opinionable
import fr.dcproject.component.opinion.entity.Opinions
import fr.dcproject.component.vote.entity.Votable
import fr.dcproject.component.vote.entity.VotableImp
import fr.dcproject.component.workgroup.WorkgroupCart
import fr.dcproject.component.workgroup.WorkgroupCartI
import fr.dcproject.component.workgroup.WorkgroupRef
import fr.dcproject.component.workgroup.WorkgroupSimple
import fr.dcproject.entity.CreatedBy
import fr.dcproject.entity.TargetI
import fr.dcproject.entity.TargetRef
import fr.dcproject.entity.VersionableRef
import fr.postgresjson.entity.EntityCreatedAt
import fr.postgresjson.entity.EntityCreatedAtImp
import fr.postgresjson.entity.EntityDeletedAt
import fr.postgresjson.entity.EntityDeletedAtImp
import fr.postgresjson.entity.EntityVersioning
import fr.postgresjson.entity.UuidEntityI
import org.joda.time.DateTime
import java.util.UUID

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
    VersionableRef,
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
    override val id: UUID = UUID.randomUUID(),
    override val title: String,
    override val anonymous: Boolean = true,
    override val content: String,
    override val description: String,
    tags: List<String> = emptyList(),
    override val draft: Boolean = false,
    override val createdBy: CitizenRef,
    override val workgroup: WorkgroupRef? = null,
    override val versionId: UUID = UUID.randomUUID(),
    override val deletedAt: DateTime? = null,
) : ArticleRef(id),
    ArticleForUpdateI<CitizenRef>,
    ArticleAuthI<CitizenRef>,
    VersionableRef {
    val tags: List<String> = tags.distinct()
}

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

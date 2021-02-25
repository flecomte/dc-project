package fr.dcproject.component.article

import fr.dcproject.common.entity.CreatedAt
import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.DeletedAt
import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.common.entity.Versionable
import fr.dcproject.common.entity.VersionableId
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
    Versionable,
    CreatedAt by CreatedAt.Imp(),
    DeletedAt by DeletedAt.Imp(deletedAt),
    VersionableId,
    Opinionable,
    Votable by VotableImp() {
    val lastVersion: Boolean = false
}

interface ArticleForUpdateI<C : CitizenRef> : ArticleI, ArticleWithTitleI, VersionableId, TargetI, CreatedBy<C> {
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
    VersionableId {
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

interface ArticleI : EntityI, TargetI

interface ArticleWithTitleI : ArticleI {
    val title: String
}

interface ArticleAuthI<U : CitizenI> :
    ArticleI,
    CreatedBy<U>,
    DeletedAt {
    val draft: Boolean
}

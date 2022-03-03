package fr.dcproject.component.article.database

import fr.dcproject.common.entity.CreatedAt
import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.DeletedAt
import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.common.entity.Versionable
import fr.dcproject.common.entity.VersionableId
import fr.dcproject.component.citizen.database.CitizenCartI
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.opinion.entity.Opinionable
import fr.dcproject.component.opinion.entity.Opinions
import fr.dcproject.component.vote.entity.Votable
import fr.dcproject.component.vote.entity.VotableImp
import fr.dcproject.component.workgroup.database.WorkgroupCart
import fr.dcproject.component.workgroup.database.WorkgroupCartI
import fr.dcproject.component.workgroup.database.WorkgroupRef
import org.joda.time.DateTime
import java.util.UUID

data class ArticleForView(
    override val id: UUID = UUID.randomUUID(),
    override val title: String,
    val anonymous: Boolean = true,
    val content: String,
    val description: String,
    val tags: List<String> = emptyList(),
    override val createdBy: CitizenCreator,
    override val versionNumber: Int = 0,
    override val versionId: UUID = UUID.randomUUID(),
    val workgroup: WorkgroupCart? = null,
    override val opinions: Opinions = emptyMap(),
    override val draft: Boolean = false,
    override val deletedAt: DateTime? = null
) : ArticleRef(id),
    ArticleAuthI<CitizenCreator>,
    ArticleWithTitleI,
    Versionable,
    CreatedAt by CreatedAt.Imp(),
    DeletedAt by DeletedAt.Imp(deletedAt),
    VersionableId,
    Opinionable,
    Votable by VotableImp() {
    val lastVersion: Boolean = false
}

sealed interface ArticleForUpdateI<C : CitizenRef> : ArticleI, ArticleWithTitleI, VersionableId, TargetI, CreatedBy<C> {
    val anonymous: Boolean
    val content: String
    val description: String
    val draft: Boolean
    val workgroup: WorkgroupRef?
}

data class ArticleForUpdate(
    override val id: UUID = UUID.randomUUID(),
    override val title: String,
    override val anonymous: Boolean = true,
    override val content: String,
    override val description: String,
    val tags: Set<String> = emptySet(),
    override val draft: Boolean = false,
    override val createdBy: CitizenRef,
    override val workgroup: WorkgroupRef? = null,
    override val versionId: UUID = UUID.randomUUID(),
    override val deletedAt: DateTime? = null,
) : ArticleRef(id),
    ArticleForUpdateI<CitizenRef>,
    ArticleAuthI<CitizenRef>,
    VersionableId

data class ArticleForListing(
    override val id: UUID = UUID.randomUUID(),
    override val title: String,
    override val createdBy: CitizenCreator,
    override val workgroup: WorkgroupCart? = null,
    override val deletedAt: DateTime? = null,
    override val draft: Boolean = false,
    val lastVersion: Boolean = false
) : ArticleForListingI,
    ArticleRef(id),
    ArticleAuthI<CitizenCartI>,
    Votable by VotableImp(),
    CreatedAt by CreatedAt.Imp(),
    CreatedBy<CitizenCartI>

sealed interface ArticleForListingI : ArticleWithTitleI, CreatedBy<CitizenCartI> {
    val workgroup: WorkgroupCartI?
}

open class ArticleRef(
    id: UUID? = null
) : ArticleI, TargetRef(id)

sealed interface ArticleI : EntityI, TargetI

sealed interface ArticleWithTitleI : ArticleI {
    val title: String
}

sealed interface ArticleAuthI<U : CitizenI> :
    ArticleI,
    CreatedBy<U>,
    DeletedAt {
    val draft: Boolean
}

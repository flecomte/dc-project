package fr.dcproject.entity

import fr.postgresjson.entity.immutable.*
import fr.postgresjson.entity.mutable.EntityDeletedAt
import fr.postgresjson.entity.mutable.EntityDeletedAtImp
import fr.postgresjson.entity.mutable.EntityVersioning
import fr.postgresjson.entity.mutable.UuidEntityVersioning
import java.util.*

class Article(
    id: UUID? = null,
    title: String,
    override var anonymous: Boolean = true,
    override var content: String,
    override var description: String,
    override var tags: List<String> = emptyList(),
    draft: Boolean = false,
    override var lastVersion: Boolean = false,
    override val createdBy: CitizenBasic,
    workgroup: WorkgroupSimple<CitizenRef>? = null
) : ArticleFull,
    ArticleAuthI<CitizenBasicI>,
    ArticleSimple(id, title, createdBy, draft, workgroup),
    Viewable by ViewableImp() {
    init {
        tags = tags.distinct()
    }
}

class ArticleForUpdate(
    id: UUID?,
    val title: String,
    val anonymous: Boolean = true,
    val content: String,
    val description: String,
    tags: List<String> = emptyList(),
    val draft: Boolean = false,
    val createdBy: CitizenRef,
    val workgroup: WorkgroupRef? = null
) : ArticleRefVersioning(id) {
    val tags: List<String> = tags.distinct()
}

open class ArticleSimple(
    id: UUID? = null,
    override var title: String,
    override val createdBy: CitizenBasic,
    override var draft: Boolean = false,
    override var workgroup: WorkgroupSimple<CitizenRef>? = null
) : ArticleSimpleI,
    ArticleAuthI<CitizenBasicI>,
    ArticleRefVersioning(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<CitizenBasicI> by EntityCreatedByImp(createdBy),
    EntityDeletedAt by EntityDeletedAtImp(),
    Votable by VotableImp(),
    Opinionable by OpinionableImp()

open class ArticleRefVersioning(
    id: UUID? = null,
    versionNumber: Int? = null,
    versionId: UUID = UUID.randomUUID()
) : ArticleRef(id),
    EntityVersioning<UUID, Int> by UuidEntityVersioning(versionNumber, versionId)

open class ArticleRef(
    id: UUID? = null
) : ArticleI, TargetRef(id)

interface ArticleI : UuidEntityI, TargetI

interface ArticleSimpleI :
    ArticleI,
    EntityVersioning<UUID, Int>,
    EntityCreatedBy<CitizenBasicI>,
    EntityCreatedAt,
    EntityDeletedAt,
    Votable {
    var title: String
    var workgroup: WorkgroupSimple<CitizenRef>?
}

interface ArticleBasicI :
    ArticleSimpleI {
    var anonymous: Boolean
    var content: String
    var description: String
    var tags: List<String>
}

interface ArticleFull :
    ArticleBasicI {
    var draft: Boolean
    var lastVersion: Boolean
}

interface ArticleAuthI<U : CitizenWithUserI> :
    ArticleI,
    EntityCreatedBy<U>,
    EntityDeletedAt {
    var draft: Boolean
}
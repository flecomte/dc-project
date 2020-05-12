package fr.dcproject.entity

import fr.postgresjson.entity.immutable.*
import fr.postgresjson.entity.mutable.EntityDeletedAt
import fr.postgresjson.entity.mutable.EntityDeletedAtImp
import fr.postgresjson.entity.mutable.EntityVersioning
import fr.postgresjson.entity.mutable.UuidEntityVersioning
import java.util.*

class Article(
    id: UUID = UUID.randomUUID(),
    title: String,
    override var anonymous: Boolean = true,
    override var content: String,
    override var description: String,
    override var tags: List<String> = emptyList(),
    draft: Boolean = false,
    override var lastVersion: Boolean = false,
    override val createdBy: CitizenBasic
) : ArticleFull,
    ArticleAuthI<CitizenBasicI>,
    ArticleSimple(id, title, createdBy, draft),
    Viewable by ViewableImp() {
    init {
        tags = tags.distinct()
    }
}

open class ArticleSimple(
    id: UUID = UUID.randomUUID(),
    override var title: String,
    override val createdBy: CitizenBasic,
    override var draft: Boolean = false
) : ArticleSimpleI,
    ArticleAuthI<CitizenBasicI>,
    ArticleRefVersioning(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<CitizenBasicI> by EntityCreatedByImp(createdBy),
    EntityDeletedAt by EntityDeletedAtImp(),
    Votable by VotableImp(),
    Opinionable by OpinionableImp()

open class ArticleRefVersioning(
    id: UUID = UUID.randomUUID(),
    versionNumber: Int? = null,
    versionId: UUID = UUID.randomUUID()
) : ArticleRef(id),
    EntityVersioning<UUID, Int> by UuidEntityVersioning(versionNumber, versionId)

open class ArticleRef(
    id: UUID = UUID.randomUUID()
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
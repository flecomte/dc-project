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
    anonymous: Boolean = true,
    content: String,
    description: String,
    tags: List<String> = emptyList(),
    override var draft: Boolean = false,
    override var lastVersion: Boolean = false,
    createdBy: CitizenBasic
) : ArticleFull,
    ArticleBasic(id, title, anonymous, content, description, tags, createdBy)

open class ArticleBasic(
    id: UUID = UUID.randomUUID(),
    title: String,
    override var anonymous: Boolean = true,
    override var content: String,
    override var description: String,
    override var tags: List<String> = emptyList(),
    override val createdBy: CitizenBasic
) : ArticleBasicI,
    ArticleSimple(id, title, createdBy) {

    init {
        tags = tags.distinct()
    }
}

open class ArticleSimple(
    id: UUID = UUID.randomUUID(),
    override var title: String,
    override val createdBy: CitizenBasic
) : ArticleSimpleI,
    ArticleRef(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<CitizenBasicI> by EntityCreatedByImp(createdBy),
    EntityDeletedAt by EntityDeletedAtImp(),
    EntityVersioning<UUID, Int> by UuidEntityVersioning(),
    Votable by VotableImp()

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

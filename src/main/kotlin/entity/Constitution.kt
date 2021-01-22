package fr.dcproject.entity

import fr.dcproject.component.article.ArticleForListing
import fr.dcproject.component.article.ArticleI
import fr.dcproject.component.citizen.CitizenSimple
import fr.dcproject.component.citizen.CitizenWithUserI
import fr.postgresjson.entity.EntityCreatedAt
import fr.postgresjson.entity.EntityCreatedAtImp
import fr.postgresjson.entity.EntityCreatedBy
import fr.postgresjson.entity.EntityCreatedByImp
import fr.postgresjson.entity.EntityDeletedAt
import fr.postgresjson.entity.EntityDeletedAtImp
import fr.postgresjson.entity.EntityVersioning
import fr.postgresjson.entity.UuidEntity
import fr.postgresjson.entity.UuidEntityVersioning
import java.util.UUID

class Constitution(
    id: UUID = UUID.randomUUID(),
    title: String,
    anonymous: Boolean = true,
    titles: MutableList<TitleSimple<ArticleForListing>> = mutableListOf(),
    draft: Boolean = false,
    lastVersion: Boolean = false,
    override val createdBy: CitizenSimple
) : ConstitutionSimple<CitizenSimple, ConstitutionSimple.TitleSimple<ArticleForListing>>(
    id,
    title = title,
    anonymous = anonymous,
    titles = titles,
    draft = draft,
    lastVersion = lastVersion,
    createdBy = createdBy
) {

    class Title(
        id: UUID = UUID.randomUUID(),
        name: String,
        rank: Int? = null,
        override val articles: MutableList<ArticleForListing> = mutableListOf()
    ) : ConstitutionSimple.TitleSimple<ArticleForListing>(id, name, rank)
}

open class ConstitutionSimple<Cr : CitizenWithUserI, T : ConstitutionSimple.TitleSimple<*>>(
    id: UUID = UUID.randomUUID(),
    val title: String,
    val anonymous: Boolean = true,
    val titles: MutableList<T> = mutableListOf(),
    val draft: Boolean = false,
    val lastVersion: Boolean = false,
    override val createdBy: Cr,
    versionId: UUID = UUID.randomUUID()
) : ConstitutionRef(id),
    EntityVersioning<UUID, Int> by UuidEntityVersioning(versionId = versionId, versionNumber = 0),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<Cr> by EntityCreatedByImp(createdBy),
    EntityDeletedAt by EntityDeletedAtImp() {

    init {
        titles.forEachIndexed { index, title ->
            title.rank = index
        }
    }

    open class TitleSimple<A : ArticleI>(
        id: UUID = UUID.randomUUID(),
        var name: String,
        var rank: Int? = null,
        open val articles: MutableList<A> = mutableListOf()
    ) : TitleRef(id)
}

open class ConstitutionRef(id: UUID = UUID.randomUUID()) : ConstitutionS(id) {
    open class TitleRef(
        id: UUID = UUID.randomUUID()
    ) : UuidEntity(id)
}

sealed class ConstitutionS(id: UUID = UUID.randomUUID()) : TargetRef(id), TargetI

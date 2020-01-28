package fr.dcproject.entity

import fr.postgresjson.entity.immutable.*
import fr.postgresjson.entity.mutable.EntityDeletedAt
import fr.postgresjson.entity.mutable.EntityDeletedAtImp
import java.util.*

class Constitution(
    id: UUID = UUID.randomUUID(),
    title: String,
    anonymous: Boolean = true,
    titles: MutableList<TitleSimple<ArticleSimple>> = mutableListOf(),
    draft: Boolean = false,
    lastVersion: Boolean = false,
    override val createdBy: CitizenSimple
) : ConstitutionSimple<CitizenSimple, ConstitutionSimple.TitleSimple<ArticleSimple>>(
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
        override val articles: MutableList<ArticleSimple> = mutableListOf()
    ) : ConstitutionSimple.TitleSimple<ArticleSimple>(id, name, rank)
}

open class ConstitutionSimple<Cr : CitizenRef, T : ConstitutionSimple.TitleSimple<*>>(
    id: UUID = UUID.randomUUID(),
    var title: String,
    var anonymous: Boolean = true,
    open var titles: MutableList<T> = mutableListOf(),
    var draft: Boolean = false,
    var lastVersion: Boolean = false,
    override val createdBy: Cr,
    versionId: UUID = UUID.randomUUID()
) : ConstitutionRef(id),
    EntityVersioning<UUID, Int?> by UuidEntityVersioning(versionId = versionId),
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
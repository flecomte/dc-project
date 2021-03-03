package fr.dcproject.component.constitution

import fr.dcproject.common.entity.CreatedAt
import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.DeletedAt
import fr.dcproject.common.entity.Entity
import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.common.entity.VersionableId
import fr.dcproject.component.article.ArticleForListing
import fr.dcproject.component.article.ArticleI
import fr.dcproject.component.citizen.CitizenCreator
import fr.dcproject.component.citizen.CitizenWithUserI
import fr.dcproject.component.constitution.ConstitutionSimple.TitleSimple
import java.util.UUID

class Constitution(
    id: UUID = UUID.randomUUID(),
    title: String,
    anonymous: Boolean = true,
    titles: MutableList<TitleSimple<ArticleForListing>> = mutableListOf(),
    draft: Boolean = false,
    lastVersion: Boolean = false,
    override val createdBy: CitizenCreator
) : ConstitutionSimple<CitizenCreator, TitleSimple<ArticleForListing>>(
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
    ) : TitleSimple<ArticleForListing>(id, name, rank)
}

open class ConstitutionSimple<Cr : CitizenWithUserI, T : TitleSimple<*>>(
    id: UUID = UUID.randomUUID(),
    val title: String,
    val anonymous: Boolean = true,
    val titles: List<T> = listOf(),
    val draft: Boolean = false,
    val lastVersion: Boolean = false,
    override val createdBy: Cr,
    versionId: UUID = UUID.randomUUID()
) : ConstitutionRef(id),
    VersionableId by VersionableId.Imp(versionId),
    CreatedAt by CreatedAt.Imp(),
    CreatedBy<Cr> by CreatedBy.Imp(createdBy),
    DeletedAt by DeletedAt.Imp() {

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

open class ConstitutionRef(id: UUID? = null) : ConstitutionS(id ?: UUID.randomUUID()) {
    open class TitleRef(
        id: UUID = UUID.randomUUID()
    ) : Entity(id)
}

sealed class ConstitutionS(id: UUID = UUID.randomUUID()) : TargetRef(id), TargetI

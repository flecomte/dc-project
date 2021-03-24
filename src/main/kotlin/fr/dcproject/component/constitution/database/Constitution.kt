package fr.dcproject.component.constitution.database

import fr.dcproject.common.entity.CreatedAt
import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.DeletedAt
import fr.dcproject.common.entity.Entity
import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.common.entity.VersionableId
import fr.dcproject.component.article.database.ArticleForListing
import fr.dcproject.component.article.database.ArticleI
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenWithUserI
import fr.dcproject.component.constitution.database.ConstitutionForUpdate.TitleForUpdate
import java.util.UUID

data class ConstitutionForView(
    override val id: UUID = UUID.randomUUID(),
    val title: String,
    val anonymous: Boolean = true,
    val titles: List<TitleForView> = listOf(),
    val draft: Boolean = false,
    val lastVersion: Boolean = false,
    override val createdBy: CitizenCreator,
    override val versionId: UUID = UUID.randomUUID(),
) : ConstitutionRef(id),
    VersionableId,
    CreatedAt by CreatedAt.Imp(),
    CreatedBy<CitizenCreator>,
    DeletedAt by DeletedAt.Imp() {
    data class TitleForView(
        override val id: UUID = UUID.randomUUID(),
        val name: String,
        val rank: Int,
        val articles: MutableList<ArticleForListing> = mutableListOf(),
    ) : TitleRef(id)
}

data class ConstitutionForListing(
    override val id: UUID = UUID.randomUUID(),
    val title: String,
    override val createdBy: CitizenCreator,
    override val versionId: UUID = UUID.randomUUID(),
) : ConstitutionRef(id),
    VersionableId,
    CreatedAt by CreatedAt.Imp(),
    CreatedBy<CitizenCreator>,
    DeletedAt by DeletedAt.Imp()

data class ConstitutionForUpdate<Cr : CitizenWithUserI, T : TitleForUpdate<*>>(
    override val id: UUID = UUID.randomUUID(),
    val title: String,
    val anonymous: Boolean = true,
    val titles: List<T> = listOf(),
    val draft: Boolean = false,
    val lastVersion: Boolean = false,
    override val createdBy: Cr,
    override val versionId: UUID = UUID.randomUUID()
) : ConstitutionRef(id),
    VersionableId by VersionableId.Imp(versionId),
    CreatedAt by CreatedAt.Imp(),
    CreatedBy<Cr> by CreatedBy.Imp(createdBy),
    DeletedAt by DeletedAt.Imp() {

    data class TitleForUpdate<A : ArticleI>(
        override val id: UUID = UUID.randomUUID(),
        val name: String,
        val articles: List<A> = listOf()
    ) : TitleRef(id)
}

open class ConstitutionRef(id: UUID? = null) : TargetRef(id), ConstitutionI {
    open class TitleRef(
        id: UUID = UUID.randomUUID()
    ) : Entity(id)
}

interface ConstitutionI : EntityI, TargetI

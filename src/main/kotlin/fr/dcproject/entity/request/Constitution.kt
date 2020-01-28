package fr.dcproject.entity.request

import fr.dcproject.entity.ArticleRef
import fr.dcproject.entity.Citizen
import fr.dcproject.entity.CitizenSimple
import fr.dcproject.entity.ConstitutionSimple
import fr.postgresjson.entity.immutable.UuidEntity
import java.util.*

class Constitution(
    var title: String,
    var anonymous: Boolean = true,
    var titles: MutableList<Title> = mutableListOf(),
    var draft: Boolean = false,
    var lastVersion: Boolean = false,
    var versionId: UUID = UUID.randomUUID()
) {
    init {
        titles.forEachIndexed { index, title ->
            title.rank = index
        }
    }

    class Title(
        id: UUID = UUID.randomUUID(),
        var name: String,
        var rank: Int? = null,
        var articles: MutableList<ArticleRef> = mutableListOf()
    ) : UuidEntity(id) {
        fun create(): ConstitutionSimple.TitleSimple<ArticleRef> {
            return ConstitutionSimple.TitleSimple(
                id, name, rank, articles
            )
        }
    }

    fun create(createdBy: Citizen): ConstitutionSimple<CitizenSimple, ConstitutionSimple.TitleSimple<ArticleRef>> {
        return ConstitutionSimple(
            title = title,
            titles = titles.create(),
            createdBy = createdBy,
            versionId = versionId
        )
    }
}

fun List<Constitution.Title>.create(): MutableList<ConstitutionSimple.TitleSimple<ArticleRef>> =
    map { it.create() }.toMutableList()
package fr.dcproject.component.article

import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.VersionableId
import fr.dcproject.common.security.AccessControl
import fr.dcproject.common.security.AccessResponse
import fr.dcproject.component.article.database.ArticleAuthI
import fr.dcproject.component.article.database.ArticleI
import fr.dcproject.component.article.database.ArticleRepository
import fr.dcproject.component.citizen.database.CitizenI

class ArticleAccessControl(private val articleRepo: ArticleRepository) : AccessControl() {
    fun <S : ArticleAuthI<*>> canView(subjects: List<S>, citizen: CitizenI?): AccessResponse =
        canAll(subjects) { canView(it, citizen) }

    fun <S : ArticleAuthI<*>> canView(subject: S, citizen: CitizenI?): AccessResponse {
        return if (subject.isDeleted()) denied("Article is deleted", "article.deleted")
        else if (subject.draft && (citizen == null || subject.createdBy.id != citizen.id)) denied("Article is draft, but it's not yours", "article.draft.not.yours")
        else granted()
    }

    fun <S : CreatedBy<*>> canDelete(subject: S, citizen: CitizenI?): AccessResponse {
        if (citizen == null) return denied("You must be connected to create article", "article.create.notConnected")
        return if (subject.createdBy.id == citizen.id) {
            granted()
        } else {
            denied("Cannot delete article if is not yours", "article.delete.notYours")
        }
    }

    fun <S> canUpsert(subject: S, citizen: CitizenI?): AccessResponse
        where S : ArticleI,
              S : CreatedBy<*>,
              S : VersionableId {
        if (citizen == null) return denied("You must be connected to create article", "article.create.notConnected")
        /* The new Article must by created by the same citizen of the connected citizen */
        if (subject.createdBy.id == citizen.id) {
            /* The creator must be the same of the creator of preview version of article */
            val lastVersionId = articleRepo
                .findVersionsByVersionId(1, 1, subject.versionId)
                .result
                .firstOrNull()?.createdBy?.id

            return when (lastVersionId) {
                null -> granted("You can create a new Article")
                citizen.id -> granted("Last version is yours")
                else -> denied("Last version is not yours", "article.lastVersion.notYours")
            }
        }
        return denied("This article must be yours for update it", "article.update.notYours")
    }
}

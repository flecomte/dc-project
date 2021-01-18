package fr.dcproject.component.article

import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.entity.CreatedBy
import fr.dcproject.entity.VersionableRef
import fr.dcproject.voter.Voter
import fr.dcproject.voter.VoterResponse

class ArticleVoter(private val articleRepo: ArticleRepository) : Voter() {
    fun <S : ArticleAuthI<*>> canView(subjects: List<S>, citizen: CitizenI?): VoterResponse =
        canAll(subjects) { canView(it, citizen) }

    fun <S : ArticleAuthI<*>> canView(subject: S, citizen: CitizenI?): VoterResponse {
        return if (subject.isDeleted()) denied("Article is deleted", "article.deleted")
        else if (subject.draft && (citizen == null || subject.createdBy.id != citizen.id)) denied("Article is draft, but it's not yours", "article.draft.not.yours")
        else granted()
    }

    fun <S : CreatedBy<*>> canDelete(subject: S, citizen: CitizenI?): VoterResponse {
        if (citizen == null) return denied("You must be connected to create article", "article.create.notConnected")
        return if (subject.createdBy.id == citizen.id) {
            granted()
        } else {
            denied("Cannot delete article if is not yours", "article.delete.notYours")
        }
    }

    fun <S> canUpsert(subject: S, citizen: CitizenI?): VoterResponse
        where S : ArticleI,
              S : CreatedBy<*>,
              S : VersionableRef {
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

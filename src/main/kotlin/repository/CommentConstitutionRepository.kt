package fr.dcproject.repository

import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.component.comment.article.CommentArticleRepository
import fr.dcproject.component.comment.generic.CommentForView
import fr.dcproject.component.comment.generic.CommentRepositoryAbs
import fr.dcproject.component.constitution.ConstitutionRef
import fr.dcproject.entity.TargetI
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.UuidEntityI
import java.util.UUID

class CommentConstitutionRepository(requester: Requester) : CommentRepositoryAbs<ConstitutionRef>(requester) {
    override fun findById(id: UUID): CommentForView<ConstitutionRef, CitizenRef>? {
        return requester
            .getFunction("find_comment_by_id")
            .selectOne(mapOf("id" to id))
    }

    override fun findByCitizen(
        citizen: CitizenI,
        page: Int,
        limit: Int
    ): Paginated<CommentForView<ConstitutionRef, CitizenRef>> {
        return requester.run {
            getFunction("find_comments_by_citizen")
                .select(
                    page,
                    limit,
                    "created_by_id" to citizen.id,
                    "reference" to TargetI.getReference(ConstitutionRef::class)
                )
        }
    }

    override fun findByTarget(
        target: UuidEntityI,
        page: Int,
        limit: Int,
        sort: CommentArticleRepository.Sort
    ): Paginated<CommentForView<ConstitutionRef, CitizenRef>> {
        return requester.run {
            getFunction("find_comments_by_target")
                .select(
                    page,
                    limit,
                    "target_id" to target.id,
                    "sort" to sort.sql
                )
        }
    }
}

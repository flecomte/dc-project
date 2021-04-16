package fr.dcproject.component.comment.constitution.database

import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.TargetI
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenCreatorI
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.comment.generic.database.CommentForView
import fr.dcproject.component.comment.generic.database.CommentRepositoryAbs
import fr.dcproject.component.constitution.database.ConstitutionRef
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import java.util.UUID

class CommentConstitutionRepository(requester: Requester) : CommentRepositoryAbs<ConstitutionRef>(requester) {
    override fun findById(id: UUID): CommentForView<ConstitutionRef, CitizenCreatorI>? {
        return requester
            .getFunction("find_comment_by_id")
            .selectOne(mapOf("id" to id))
    }

    override fun findByCitizen(
        citizen: CitizenI,
        page: Int,
        limit: Int
    ): Paginated<CommentForView<ConstitutionRef, CitizenCreatorI>> {
        return requester.run {
            getFunction("find_comments_by_citizen")
                .select<CommentForView<ConstitutionRef, CitizenCreator>>(
                    page,
                    limit,
                    "created_by_id" to citizen.id,
                    "reference" to TargetI.getReference(ConstitutionRef::class)
                )
                as Paginated<CommentForView<ConstitutionRef, CitizenCreatorI>>
        }
    }

    override fun findByTarget(
        target: EntityI,
        page: Int,
        limit: Int,
        sort: String
    ): Paginated<CommentForView<ConstitutionRef, CitizenCreatorI>> {
        return requester.run {
            getFunction("find_comments_by_target")
                .select<CommentForView<ConstitutionRef, CitizenCreator>>(
                    page,
                    limit,
                    "target_id" to target.id,
                    "sort" to sort
                )
                as Paginated<CommentForView<ConstitutionRef, CitizenCreatorI>>
        }
    }
}

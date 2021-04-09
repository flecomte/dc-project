package fr.dcproject.component.comment.generic.database

import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenCreatorI
import fr.dcproject.component.citizen.database.CitizenI
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import java.util.UUID

abstract class CommentRepositoryAbs<T : TargetI>(override var requester: Requester) : RepositoryI {
    abstract fun findById(id: UUID): CommentForView<T, CitizenCreatorI>?

    abstract fun findByCitizen(
        citizen: CitizenI,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<CommentForView<T, CitizenCreatorI>>

    open fun findByParent(
        parent: CommentForView<T, CitizenCreatorI>,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<CommentForView<T, CitizenCreatorI>> {
        return findByParent(parent.id, page, limit)
    }

    open fun findByParent(
        parentId: UUID,
        page: Int = 1,
        limit: Int = 50
    ): Paginated<CommentForView<T, CitizenCreatorI>> {
        return requester.run {
            getFunction("find_comments_by_parent")
                .select<CommentForView<T, CitizenCreator>>(
                    page,
                    limit,
                    "parent_id" to parentId
                )
                as Paginated<CommentForView<T, CitizenCreatorI>>
        }
    }

    open fun findByTarget(
        target: EntityI,
        page: Int = 1,
        limit: Int = 50,
        sort: String = "createdAt"
    ): Paginated<CommentForView<T, CitizenCreatorI>> {
        return findByTarget(target.id, page, limit, sort)
    }

    open fun findByTarget(
        targetId: UUID,
        page: Int = 1,
        limit: Int = 50,
        sort: String = "createdAt"
    ): Paginated<CommentForView<T, CitizenCreatorI>> {
        return requester.run {
            getFunction("find_comments_by_target")
                .select<CommentForView<T, CitizenCreator>>(
                    page,
                    limit,
                    "target_id" to targetId,
                    "sort" to sort
                )
                as Paginated<CommentForView<T, CitizenCreatorI>>
        }
    }

    fun <I : TargetI, C : CitizenCreatorI> comment(comment: CommentForUpdate<I, C>) {
        requester
            .getFunction("comment")
            .sendQuery(
                "reference" to comment.target.reference,
                "resource" to comment
            )
    }

    fun <I : T> edit(comment: CommentForUpdate<I, CitizenCreatorI>) {
        requester
            .getFunction("edit_comment")
            .sendQuery(
                "id" to comment.id,
                "content" to comment.content
            )
    }
}

class CommentRepository(requester: Requester) : CommentRepositoryAbs<TargetRef>(requester) {
    override fun findById(id: UUID): CommentForView<TargetRef, CitizenCreatorI>? {
        return requester
            .getFunction("find_comment_by_id")
            .selectOne<CommentForView<TargetRef, CitizenCreator>>(mapOf("id" to id))
            as CommentForView<TargetRef, CitizenCreatorI>?
    }

    override fun findByCitizen(
        citizen: CitizenI,
        page: Int,
        limit: Int
    ): Paginated<CommentForView<TargetRef, CitizenCreatorI>> {
        return requester.run {
            getFunction("find_comments_by_citizen")
                .select<CommentForView<TargetRef, CitizenCreator>>(
                    page,
                    limit,
                    "created_by_id" to citizen.id
                ) as Paginated<CommentForView<TargetRef, CitizenCreatorI>>
        }
    }

    override fun findByParent(
        parentId: UUID,
        page: Int,
        limit: Int
    ): Paginated<CommentForView<TargetRef, CitizenCreatorI>> {
        return requester.run {
            getFunction("find_comments_by_parent")
                .select<CommentForView<TargetRef, CitizenCreator>>(
                    page,
                    limit,
                    "parent_id" to parentId
                )
                as Paginated<CommentForView<TargetRef, CitizenCreatorI>>
        }
    }
}

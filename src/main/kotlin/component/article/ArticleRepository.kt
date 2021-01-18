package fr.dcproject.component.article

import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.Parameter
import fr.postgresjson.repository.RepositoryI
import net.pearx.kasechange.toSnakeCase
import java.util.*

class ArticleRepository(override var requester: Requester) : RepositoryI {
    fun findById(id: UUID): ArticleForView? {
        val function = requester.getFunction("find_article_by_id")
        return function.selectOne("id" to id)
    }

    fun findVersionsByVersionId(page: Int = 1, limit: Int = 50, versionId: UUID): Paginated<ArticleForView> {
        return requester
            .getFunction("find_articles_versions_by_version_id")
            .select(page, limit, "version_id" to versionId)
    }

    fun find(
        page: Int = 1,
        limit: Int = 50,
        sort: String? = null,
        direction: RepositoryI.Direction? = null,
        search: String? = null,
        filter: Filter = Filter()
    ): Paginated<ArticleForListing> {
        return requester
            .getFunction("find_articles")
            .select(
                page,
                limit,
                "sort" to sort?.toSnakeCase(),
                "direction" to direction,
                "search" to search,
                "filter" to filter
            )
    }

    fun upsert(article: ArticleForUpdate): ArticleForView? {
        return requester
            .getFunction("upsert_article")
            .selectOne("resource" to article)
    }

    class Filter(
        val createdById: String? = null,
        val workgroupId: String? = null
    ) : Parameter
}

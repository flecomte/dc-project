package fr.dcproject.component.article.database

import fr.dcproject.common.entity.VersionableId
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.Parameter
import fr.postgresjson.repository.RepositoryI
import net.pearx.kasechange.toSnakeCase
import java.util.UUID

class ArticleRepository(override var requester: Requester) : RepositoryI {
    fun findById(id: UUID): ArticleForView? {
        val function = requester.getFunction("find_article_by_id")
        return function.selectOne("id" to id)
    }

    fun findVersionsById(page: Int = 1, limit: Int = 50, id: UUID): Paginated<ArticleForListing> {
        return requester
            .getFunction("find_articles_versions_by_id")
            .select(page, limit, "id" to id)
    }

    fun <A> findSiblingVersions(page: Int = 1, limit: Int = 50, article: A): Paginated<ArticleForListing> where A : VersionableId, A : ArticleI {
        return requester
            .getFunction("find_articles_versions_by_version_id")
            .select(page, limit, "version_id" to article.versionId)
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

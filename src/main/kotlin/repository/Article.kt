package fr.dcproject.repository

import fr.dcproject.entity.ArticleFull
import fr.dcproject.entity.ArticleSimple
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.Parameter
import fr.postgresjson.repository.RepositoryI
import fr.postgresjson.repository.RepositoryI.Direction
import net.pearx.kasechange.toSnakeCase
import java.util.*
import fr.dcproject.entity.Article as ArticleEntity

class Article(override var requester: Requester) : RepositoryI {
    fun findById(id: UUID): ArticleEntity? {
        val function = requester.getFunction("find_article_by_id")
        return function.selectOne("id" to id)
    }

    fun findVerionsByVersionsId(page: Int = 1, limit: Int = 50, versionId: UUID): Paginated<ArticleEntity> {
        return requester
            .getFunction("find_articles_versions_by_version_id")
            .select(page, limit, "version_id" to versionId)
    }

    fun find(
        page: Int = 1,
        limit: Int = 50,
        sort: String? = null,
        direction: Direction? = null,
        search: String? = null,
        filter: Filter = Filter()
    ): Paginated<ArticleSimple> {
        return requester
            .getFunction("find_articles")
            .select(
                page, limit,
                "sort" to sort?.toSnakeCase(),
                "direction" to direction,
                "search" to search,
                "filter" to filter
            )
    }

    fun upsert(article: ArticleFull): ArticleEntity? {
        return requester
            .getFunction("upsert_article")
            .selectOne("resource" to article)
    }

    class Filter(
        val createdById: String? = null
    ) : Parameter
}

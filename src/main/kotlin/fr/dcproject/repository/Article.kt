package fr.dcproject.repository

import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import fr.postgresjson.repository.RepositoryI.Direction
import net.pearx.kasechange.toSnakeCase
import java.util.*
import fr.dcproject.entity.Article as ArticleEntity

class Article(override var requester: Requester) : RepositoryI<ArticleEntity> {
    override val entityName = ArticleEntity::class

    fun findById(id: UUID): ArticleEntity? {
        val function = requester.getFunction("find_article_by_id")
        return function.selectOne("id" to id)
    }

    fun find(
        page: Int = 1,
        limit: Int = 50,
        sort: String? = null,
        direction: Direction? = null,
        search: String? = null
    ): Paginated<ArticleEntity> {
        return requester
            .getFunction("find_articles")
            .select(
                page, limit,
                "sort" to sort?.toSnakeCase(),
                "direction" to direction,
                "search" to search
            )
    }

    fun upsert(article: ArticleEntity): ArticleEntity? {
        return requester
            .getFunction("upsert_article")
            .selectOne("resource" to article)
    }
}

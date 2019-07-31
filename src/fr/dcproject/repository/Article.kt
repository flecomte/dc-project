package fr.dcproject.repository

import fr.postgresjson.connexion.Requester
import fr.postgresjson.entity.EntitiesCollections
import fr.postgresjson.repository.RepositoryI
import java.util.*
import fr.dcproject.entity.Article as ArticleEntity

class Article(override var requester: Requester) : RepositoryI<ArticleEntity> {
    override val entityName = ArticleEntity::class

    fun findById(id: UUID): ArticleEntity? {
        val function = requester.getFunction("find_article_by_id")
        return when (val e = EntitiesCollections().get(id) as ArticleEntity?) {
            null -> {
                function.selectOne("id" to id)
            }
            else -> e
        }
    }

    fun upsert(article: ArticleEntity): ArticleEntity? {
        return requester
            .getFunction("upsert_article")
            .selectOne<ArticleEntity>("resource" to article)?.also {
                EntitiesCollections().set(it)
            }
    }
}

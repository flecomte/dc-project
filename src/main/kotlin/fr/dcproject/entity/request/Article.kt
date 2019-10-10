package fr.dcproject.entity.request

import fr.dcproject.entity.Citizen
import java.util.*
import fr.dcproject.entity.Article as ArticleEntity

class Article(
    val id: UUID?,
    val title: String,
    val anonymous: Boolean? = true,
    val content: String,
    val description: String,
    val tags: List<String> = emptyList(),
    val draft: Boolean = false,
    val versionId: UUID?
) :
    Request {

    fun merge(article: ArticleEntity) {
        article.title = this.title
        article.content = this.content
        article.description = this.description
        article.tags = this.tags.distinct()
        article.anonymous = this.anonymous
        article.draft = this.draft
        article.versionId = this.versionId ?: UUID.randomUUID()
    }

    fun create(createdBy: Citizen): ArticleEntity {
        return ArticleEntity(
            id ?: UUID.randomUUID(),
            title,
            anonymous,
            content,
            description,
            tags,
            draft,
            createdBy = createdBy
        ).apply { this.versionId = this@Article.versionId ?: UUID.randomUUID() }
    }
}

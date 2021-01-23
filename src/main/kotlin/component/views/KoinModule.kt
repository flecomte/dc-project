package fr.dcproject.component.views

import fr.dcproject.application.Configuration
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleViewManager
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.koin.dsl.module

val viewKoinModule = module {
    // Elasticsearch Client
    val esClient = RestClient.builder(
        HttpHost.create(Configuration.elasticsearch)
    ).build().apply {
        createEsIndexForViews()
    }

    single { ArticleViewManager<ArticleForView>(esClient) }
}

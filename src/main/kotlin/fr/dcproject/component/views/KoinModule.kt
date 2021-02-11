package fr.dcproject.component.views

import fr.dcproject.application.Configuration
import fr.dcproject.component.article.ArticleForView
import fr.dcproject.component.article.ArticleViewManager
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.koin.dsl.module

val viewKoinModule = module {

    single {
        val config: Configuration = get()
        // Elasticsearch Client
        val esClient = RestClient.builder(
            HttpHost.create(config.elasticsearch)
        ).build().apply {
            createEsIndexForViews()
        }
        ArticleViewManager<ArticleForView>(esClient)
    }
}

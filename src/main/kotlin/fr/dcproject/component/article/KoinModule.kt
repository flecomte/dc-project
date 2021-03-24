package fr.dcproject.component.article

import fr.dcproject.component.article.database.ArticleRepository
import org.koin.dsl.module

val articleKoinModule = module {
    single { ArticleRepository(get()) }
    single { ArticleAccessControl(get()) }
}

package fr.dcproject.component.article

import org.koin.dsl.module

val articleKoinModule = module {
    single { ArticleRepository(get()) }
    single { ArticleAccessControl(get()) }
}

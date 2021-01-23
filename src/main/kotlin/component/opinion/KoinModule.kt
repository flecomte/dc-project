package fr.dcproject.component.opinion

import org.koin.dsl.module

val opinionKoinModule = module {
    single { OpinionChoiceRepository(get()) }
    single { OpinionRepositoryArticle(get()) }

    single { OpinionAccessControl() }
    single { OpinionChoiceAccessControl() }
}

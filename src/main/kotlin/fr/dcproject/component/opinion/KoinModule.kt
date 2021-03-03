package fr.dcproject.component.opinion

import fr.dcproject.component.opinion.database.OpinionChoiceRepository
import fr.dcproject.component.opinion.database.OpinionRepositoryArticle
import org.koin.dsl.module

val opinionKoinModule = module {
    single { OpinionChoiceRepository(get()) }
    single { OpinionRepositoryArticle(get()) }

    single { OpinionAccessControl() }
    single { OpinionChoiceAccessControl() }
}

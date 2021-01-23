package fr.dcproject.component.vote

import org.koin.dsl.module

val voteKoinModule = module {
    single { VoteRepository(get()) }
    single { VoteArticleRepository(get()) }
    single { VoteConstitutionRepository(get()) }
    single { VoteCommentRepository(get()) }

    single { VoteAccessControl() }
}

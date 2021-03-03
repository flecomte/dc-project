package fr.dcproject.component.vote

import fr.dcproject.component.vote.database.VoteArticleRepository
import fr.dcproject.component.vote.database.VoteCommentRepository
import fr.dcproject.component.vote.database.VoteConstitutionRepository
import fr.dcproject.component.vote.database.VoteRepository
import org.koin.dsl.module

val voteKoinModule = module {
    single { VoteRepository(get()) }
    single { VoteArticleRepository(get()) }
    single { VoteConstitutionRepository(get()) }
    single { VoteCommentRepository(get()) }

    single { VoteAccessControl() }
}

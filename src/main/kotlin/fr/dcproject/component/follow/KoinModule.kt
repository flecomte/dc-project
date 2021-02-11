package fr.dcproject.component.follow

import org.koin.dsl.module

val followKoinModule = module {
    single { FollowArticleRepository(get()) }
    single { FollowConstitutionRepository(get()) }
    single { FollowAccessControl() }
}

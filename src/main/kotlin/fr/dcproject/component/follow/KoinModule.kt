package fr.dcproject.component.follow

import fr.dcproject.component.follow.database.FollowArticleRepository
import fr.dcproject.component.follow.database.FollowCitizenRepository
import fr.dcproject.component.follow.database.FollowConstitutionRepository
import org.koin.dsl.module

val followKoinModule = module {
    single { FollowArticleRepository(get()) }
    single { FollowConstitutionRepository(get()) }
    single { FollowCitizenRepository(get()) }
    single { FollowAccessControl() }
}

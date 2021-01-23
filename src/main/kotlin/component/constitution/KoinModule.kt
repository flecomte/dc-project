package fr.dcproject.component.constitution

import org.koin.dsl.module

val constitutionKoinModule = module {
    single { ConstitutionRepository(get()) }
    single { ConstitutionAccessControl() }
}

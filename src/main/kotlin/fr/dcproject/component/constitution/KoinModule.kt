package fr.dcproject.component.constitution

import fr.dcproject.component.constitution.database.ConstitutionRepository
import org.koin.dsl.module

val constitutionKoinModule = module {
    single { ConstitutionRepository(get()) }
    single { ConstitutionAccessControl() }
}

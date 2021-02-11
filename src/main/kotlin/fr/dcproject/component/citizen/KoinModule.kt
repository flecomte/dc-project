package fr.dcproject.component.citizen

import org.koin.dsl.module

val citizenKoinModule = module {
    single { CitizenRepository(get()) }
    single { CitizenAccessControl() }
}

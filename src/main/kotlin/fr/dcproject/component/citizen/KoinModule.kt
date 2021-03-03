package fr.dcproject.component.citizen

import fr.dcproject.component.citizen.database.CitizenRepository
import org.koin.dsl.module

val citizenKoinModule = module {
    single { CitizenRepository(get()) }
    single { CitizenAccessControl() }
}

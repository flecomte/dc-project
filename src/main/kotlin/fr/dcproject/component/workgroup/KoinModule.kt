package fr.dcproject.component.workgroup

import fr.dcproject.component.workgroup.database.WorkgroupRepository
import org.koin.dsl.module

val workgroupKoinModule = module {
    single { WorkgroupRepository(get()) }
    single { WorkgroupAccessControl() }
}

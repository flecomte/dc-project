package fr.dcproject.component.workgroup

import org.koin.dsl.module

val workgroupKoinModule = module {
    single { WorkgroupRepository(get()) }
    single { WorkgroupAccessControl() }
}

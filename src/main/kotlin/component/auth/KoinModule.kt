package fr.dcproject.component.auth

import fr.dcproject.application.Configuration
import fr.dcproject.common.email.Mailer
import org.koin.dsl.module

val authKoinModule = module {
    single { UserRepository(get()) }
    // Used to send a connexion link by email
    single {
        val config: Configuration = get()
        PasswordlessAuth(get<Mailer>(), config.domain, get())
    }
}

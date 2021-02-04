package fr.dcproject.component.auth

import fr.dcproject.application.Configuration
import fr.dcproject.messages.Mailer
import org.koin.dsl.module

val authKoinModule = module {
    single { UserRepository(get()) }
    // Used to send a connexion link by email
    single {
        val config: Configuration = get()
        PasswordlessAuth(get<Mailer>(), config.domain, get())
    }
}

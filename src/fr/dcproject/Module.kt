package fr.dcproject

import fr.postgresjson.connexion.Requester
import io.ktor.util.KtorExperimentalAPI
import org.koin.dsl.module
import java.io.File
import fr.dcproject.repository.Article as ArticleRepository

val config = Config()

@KtorExperimentalAPI
val Module = module {

    single { config }

    single { Requester.RequesterFactory(
        host = config.host,
        database = config.database,
        username = config.username,
        password = config.password,
        port = config.port,
        functionsDirectory = File(this::class.java.getResource("/sql/functions").toURI())
    ).createRequester() }

    single { ArticleRepository(get<Requester>()) }
}

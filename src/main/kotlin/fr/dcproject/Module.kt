package fr.dcproject

import fr.postgresjson.connexion.Connection
import fr.postgresjson.connexion.Requester
import fr.postgresjson.migration.Migrations
import io.ktor.util.KtorExperimentalAPI
import org.koin.dsl.module
import fr.dcproject.repository.Article as ArticleRepository

val config = Config()

@KtorExperimentalAPI
val Module = module {

    single { config }

    single { Connection(host = config.host, port = config.port, database = config.database, username = config.username, password = config.password) }

    single { Requester.RequesterFactory(
        connection = get(),
        functionsDirectory = config.sqlFiles.resolve("functions")
    ).createRequester() }

    single { ArticleRepository(get()) }
    single { Migrations(connection = get(), directory = config.sqlFiles) }
}

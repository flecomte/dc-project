package fr.dcproject

import fr.dcproject.messages.Mailer
import fr.dcproject.messages.SsoManager
import fr.postgresjson.connexion.Connection
import fr.postgresjson.connexion.Requester
import fr.postgresjson.migration.Migrations
import io.ktor.util.KtorExperimentalAPI
import org.koin.dsl.module
import fr.dcproject.repository.Article as ArticleRepository
import fr.dcproject.repository.Citizen as CitizenRepository
import fr.dcproject.repository.CommentArticle as CommentArticleRepository
import fr.dcproject.repository.CommentConstitution as CommentConstitutionRepository
import fr.dcproject.repository.CommentGeneric as CommentGenericRepository
import fr.dcproject.repository.Constitution as ConstitutionRepository
import fr.dcproject.repository.FollowArticle as FollowArticleRepository
import fr.dcproject.repository.FollowConstitution as FollowConstitutionRepository
import fr.dcproject.repository.OpinionArticle as OpinionArticleRepository
import fr.dcproject.repository.OpinionChoice as OpinionChoiceRepository
import fr.dcproject.repository.User as UserRepository
import fr.dcproject.repository.VoteArticle as VoteArticleRepository
import fr.dcproject.repository.VoteComment as VoteCommentRepository
import fr.dcproject.repository.VoteConstitution as VoteConstitutionRepository

val config = Config()

@KtorExperimentalAPI
val Module = module {

    single { config }

    single {
        Connection(
            host = config.host,
            port = config.port,
            database = config.database,
            username = config.username,
            password = config.password
        )
    }

    single {
        Requester.RequesterFactory(
            connection = get(),
            functionsDirectory = config.sqlFiles.resolve("functions")
        ).createRequester()
    }

    // TODO: create generic declaration
    single { UserRepository(get()) }
    single { ArticleRepository(get()) }
    single { CitizenRepository(get()) }
    single { ConstitutionRepository(get()) }
    single { FollowArticleRepository(get()) }
    single { FollowConstitutionRepository(get()) }
    single { CommentGenericRepository(get()) }
    single { CommentArticleRepository(get()) }
    single { CommentConstitutionRepository(get()) }
    single { VoteArticleRepository(get()) }
    single { VoteConstitutionRepository(get()) }
    single { VoteCommentRepository(get()) }
    single { OpinionChoiceRepository(get()) }
    single { OpinionArticleRepository(get()) }

    single { Migrations(connection = get(), directory = config.sqlFiles) }

    single { Mailer(config.sendGridKey) }
    single { SsoManager(get<Mailer>(), config.domain, get()) }
}

package fr.dcproject.component.comment

import fr.dcproject.component.comment.article.database.CommentArticleRepository
import fr.dcproject.component.comment.constitution.database.CommentConstitutionRepository
import fr.dcproject.component.comment.generic.CommentAccessControl
import fr.dcproject.component.comment.generic.database.CommentRepository
import org.koin.dsl.module

val commentKoinModule = module {
    single { CommentRepository(get()) }
    single { CommentArticleRepository(get()) }
    single { CommentConstitutionRepository(get()) }
    single { CommentAccessControl() }
}

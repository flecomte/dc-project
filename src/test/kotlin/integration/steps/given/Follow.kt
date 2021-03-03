package integration.steps.given

import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.citizen.database.Citizen
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.citizen.database.CitizenRepository
import fr.dcproject.component.constitution.database.ConstitutionRef
import fr.dcproject.component.follow.database.FollowArticleRepository
import fr.dcproject.component.follow.database.FollowConstitutionRepository
import fr.dcproject.component.follow.database.FollowForUpdate
import io.ktor.server.testing.TestApplicationEngine
import org.koin.core.context.GlobalContext

fun Citizen.`And follow article`(
    article: String,
) {
    createFollow(this, ArticleRef(article.toUUID()))
}

fun Citizen.`And follow constitution`(
    constitution: String,
) {
    createFollow(this, ConstitutionRef(constitution.toUUID()))
}

fun TestApplicationEngine.`Given I have follow on article`(
    firstName: String,
    lastName: String,
    article: String,
) {
    val citizenRepository: CitizenRepository by lazy { GlobalContext.get().koin.get() }
    val citizen = citizenRepository.findByUsername("$firstName-$lastName".toLowerCase()) ?: error("Citizen not exist")
    createFollow(citizen, ArticleRef(article.toUUID()))
}

fun TestApplicationEngine.`Given I have follow on constitution`(
    firstName: String,
    lastName: String,
    constitution: String,
) {
    val citizenRepository: CitizenRepository by lazy { GlobalContext.get().koin.get() }
    val citizen = citizenRepository.findByUsername("$firstName-$lastName".toLowerCase()) ?: error("Citizen not exist")
    createFollow(citizen, ArticleRef(constitution.toUUID()))
}

fun createFollow(citizen: CitizenRef, article: ArticleRef) {
    val followArticleRepository: FollowArticleRepository by lazy { GlobalContext.get().koin.get() }
    val follow = FollowForUpdate(createdBy = citizen, target = article)
    followArticleRepository.follow(follow)
}

fun createFollow(citizen: CitizenRef, constitution: ConstitutionRef) {
    val followConstitutionRepository: FollowConstitutionRepository by lazy { GlobalContext.get().koin.get() }
    val follow = FollowForUpdate(createdBy = citizen, target = constitution)
    followConstitutionRepository.follow(follow)
}

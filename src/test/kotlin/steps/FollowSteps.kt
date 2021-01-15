package steps

import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.citizen.CitizenRepository
import fr.dcproject.entity.ConstitutionRef
import fr.dcproject.entity.FollowForUpdate
import fr.dcproject.utils.toUUID
import io.cucumber.java8.En
import org.koin.test.KoinTest
import org.koin.test.get
import fr.dcproject.repository.FollowArticle as FollowArticleRepository
import fr.dcproject.repository.FollowConstitution as FollowConstitutionRepository

class FollowSteps : En, KoinTest {
    init {
        Given("I have follow of {word} {word} on article {string}") { firstName: String, lastName: String, articleId: String ->
            val username = "$firstName-$lastName".toLowerCase()
            val citizen = get<CitizenRepository>().findByUsername(username) ?: error("Citizen not exist")
            val follow = FollowForUpdate(createdBy = citizen, target = ArticleRef(articleId.toUUID()))
            get<FollowArticleRepository>().follow(follow)
        }
        Given("I have follow of {word} {word} on constitution {string}") { firstName: String, lastName: String, constitutionId: String ->
            val username = "$firstName-$lastName".toLowerCase()
            val citizen = get<CitizenRepository>().findByUsername(username) ?: error("Citizen not exist")
            val follow = FollowForUpdate(createdBy = citizen, target = ConstitutionRef(constitutionId.toUUID()))
            get<FollowConstitutionRepository>().follow(follow)
        }
    }
}
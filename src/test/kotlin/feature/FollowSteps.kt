package feature

import fr.dcproject.entity.ArticleRef
import fr.dcproject.entity.Follow
import fr.dcproject.utils.toUUID
import io.cucumber.java8.En
import org.koin.test.KoinTest
import org.koin.test.get
import fr.dcproject.repository.Citizen as CitizenRepository
import fr.dcproject.repository.FollowArticle as FollowArticleRepository

class FollowSteps : En, KoinTest {
    init {
        Given("The citizen {word} {word} follow article {string}") { firstName: String, lastName: String, articleId: String ->
            val username = "$firstName-$lastName".toLowerCase()
            val citizen = get<CitizenRepository>().findByUsername(username) ?: error("Citizen not exist")
            val follow = Follow(createdBy = citizen, target = ArticleRef(articleId.toUUID()))
            get<FollowArticleRepository>().follow(follow)
        }
    }
}
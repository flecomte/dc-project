package integration.steps.given

import com.thedeanda.lorem.LoremIpsum
import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.citizen.database.CitizenI.Name
import fr.dcproject.component.citizen.database.CitizenWithUserI
import fr.dcproject.component.constitution.database.Constitution
import fr.dcproject.component.constitution.database.ConstitutionRepository
import fr.dcproject.component.constitution.database.ConstitutionSimple
import fr.dcproject.component.constitution.database.ConstitutionSimple.TitleSimple
import io.ktor.server.testing.TestApplicationEngine
import org.koin.core.context.GlobalContext
import java.util.UUID

fun TestApplicationEngine.`Given I have constitution`(
    id: String? = null,
    titles: List<TitleSimple<ArticleRef>>? = null,
    createdBy: Name? = null
) {
    createConstitution(id?.toUUID(), titles, createdBy)
}

fun TestApplicationEngine.`Given I have constitutions`(
    numbers: Int,
) {
    repeat(numbers) {
        createConstitution()
    }
}

fun createTitles(nbr: Int): List<TitleSimple<ArticleRef>> = sequence {
    repeat(nbr) {
        yield(createTitle())
    }
}.toList()

fun createTitle(): TitleSimple<ArticleRef> {
    return TitleSimple(name = LoremIpsum().getTitle(3))
}

fun createConstitution(
    id: UUID? = null,
    titles: List<TitleSimple<ArticleRef>>? = null,
    createdBy: Name? = null
): Constitution {
    val constitutionRepository: ConstitutionRepository by lazy { GlobalContext.get().koin.get() }

    val creator: CitizenWithUserI = createCitizen(createdBy)

    val constitution = ConstitutionSimple(
        id = id ?: UUID.randomUUID(),
        title = LoremIpsum().getTitle(3),
        titles = titles ?: createTitles(5),
        createdBy = creator,
        versionId = UUID.randomUUID()
    )
    return constitutionRepository.upsert(constitution) ?: error("Cannot create constitution")
}

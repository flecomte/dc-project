package integration.steps.given

import com.thedeanda.lorem.LoremIpsum
import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.citizen.database.CitizenI.Name
import fr.dcproject.component.citizen.database.CitizenWithUserI
import fr.dcproject.component.constitution.database.ConstitutionForUpdate
import fr.dcproject.component.constitution.database.ConstitutionForUpdate.TitleForUpdate
import fr.dcproject.component.constitution.database.ConstitutionForView
import fr.dcproject.component.constitution.database.ConstitutionRepository
import io.ktor.server.testing.TestApplicationEngine
import org.koin.core.context.GlobalContext
import java.util.UUID

fun TestApplicationEngine.`Given I have constitution`(
    id: String? = null,
    titles: List<TitleForUpdate<ArticleRef>>? = null,
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

fun createTitles(nbr: Int): List<TitleForUpdate<ArticleRef>> = sequence {
    repeat(nbr) {
        yield(createTitle())
    }
}.toList()

fun createTitle(): TitleForUpdate<ArticleRef> {
    return TitleForUpdate(name = LoremIpsum().getTitle(3))
}

fun createConstitution(
    id: UUID? = null,
    titles: List<TitleForUpdate<ArticleRef>>? = null,
    createdBy: Name? = null
): ConstitutionForView {
    val constitutionRepository: ConstitutionRepository by lazy { GlobalContext.get().koin.get() }

    val creator: CitizenWithUserI = createCitizen(createdBy)

    val constitution = ConstitutionForUpdate(
        id = id ?: UUID.randomUUID(),
        title = LoremIpsum().getTitle(3),
        titles = titles ?: createTitles(5),
        createdBy = creator,
        versionId = UUID.randomUUID()
    )
    return constitutionRepository.upsert(constitution) ?: error("Cannot create constitution")
}

package integration.steps.given

import com.thedeanda.lorem.LoremIpsum
import fr.dcproject.common.utils.toUUID
import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.citizen.CitizenWithUserI
import fr.dcproject.component.constitution.Constitution
import fr.dcproject.component.constitution.ConstitutionRepository
import fr.dcproject.component.constitution.ConstitutionSimple
import fr.dcproject.component.constitution.ConstitutionSimple.TitleSimple
import io.ktor.server.testing.TestApplicationEngine
import org.koin.core.context.GlobalContext
import java.util.UUID

fun TestApplicationEngine.`Given I have constitution`(
    id: String? = null,
    titles: List<TitleSimple<ArticleRef>>? = null,
    createdByUsername: String? = null
) {
    createConstitution(id?.toUUID(), titles, createdByUsername)
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
    createdByUsername: String? = null
): Constitution {
    val constitutionRepository: ConstitutionRepository by lazy { GlobalContext.get().koin.get() }

    val createdBy: CitizenWithUserI = createCitizen(createdByUsername)

    val constitution = ConstitutionSimple(
        id = id ?: UUID.randomUUID(),
        title = LoremIpsum().getTitle(3),
        titles = titles ?: createTitles(5),
        createdBy = createdBy,
        versionId = UUID.randomUUID()
    )
    return constitutionRepository.upsert(constitution) ?: error("Cannot create constitution")
}

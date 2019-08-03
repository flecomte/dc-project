package fr.dcproject.entity
import fr.postgresjson.entity.*
import java.util.*


class Article(
    id: UUID = UUID.randomUUID(),
    var versionId: UUID = UUID.randomUUID(),
    var versionNumber: Int? = null,
    var title: String?,
    var annonymous: Boolean? = true,
    var content: String?,
    var description: String?,
    var tags: List<String> = emptyList(),
    override var createdBy: Citizen?
):
    UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    CreatedBy<Citizen> by EntityCreatedByImp()
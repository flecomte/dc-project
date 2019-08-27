package fr.dcproject.entity
import fr.postgresjson.entity.*
import java.util.*


class Article(
    id: UUID = UUID.randomUUID(),
    var title: String?,
    var annonymous: Boolean? = true,
    var content: String?,
    var description: String?,
    var tags: List<String> = emptyList(),
    createdBy: Citizen?
):
    UuidEntity(id),
    EntityVersioning<UUID, Int> by UuidEntityVersioning(),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<Citizen> by EntityCreatedByImp(createdBy)
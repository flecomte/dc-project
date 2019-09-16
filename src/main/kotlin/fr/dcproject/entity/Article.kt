package fr.dcproject.entity
import fr.postgresjson.entity.*
import java.util.*


class Article(
    id: UUID = UUID.randomUUID(),
    var title: String?,
    var anonymous: Boolean? = true,
    var content: String?,
    var description: String?,
    var tags: List<String> = emptyList(),
    var draft: Boolean = false,
    var lastVersion: Boolean = false,
    createdBy: Citizen?
):
    UuidEntity(id),
    EntityVersioning<UUID, Int> by UuidEntityVersioning(),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<Citizen> by EntityCreatedByImp(createdBy),
    EntityDeletedAt by EntityDeletedAtImp() {
    init {
        tags = tags.distinct()
    }
}
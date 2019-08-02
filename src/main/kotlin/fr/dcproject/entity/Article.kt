package fr.dcproject.entity
import fr.postgresjson.entity.*
import java.util.*


class Article(
    id: UUID?,
    var versionId: UUID?,
    var versionNumber: Int?,
    var title: String?,
    var annonymous: Boolean?,
    var content: String?,
    var description: String?,
    var tags: List<String>
):
    UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    CreatedBy<Citizen> by EntityCreatedByImp()
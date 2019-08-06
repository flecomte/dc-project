package fr.dcproject.entity

import fr.postgresjson.entity.*
import java.util.*

class Constitution(
    id: UUID = UUID.randomUUID(),
    var title: String?,
    var annonymous: Boolean?,
    var titles: List<Title>,
    createdBy: Citizen?
):  UuidEntity(id),
    EntityVersioning<UUID, Int> by UuidEntityVersioning(),
    EntityCreatedAt by EntityCreatedAtImp(),
    CreatedBy<Citizen> by EntityCreatedByImp(createdBy) {

    init{
        titles.forEachIndexed { index, title ->
            title.createdBy = this.createdBy
            title.rank = index
        }
    }

    class Title(
        id: UUID = UUID.randomUUID(),
        var name: String?,
        var rank: Int? = null,
        createdBy: Citizen? = null
    ):  UuidEntity(id),
        EntityCreatedAt by EntityCreatedAtImp(),
        CreatedBy<Citizen> by EntityCreatedByImp(createdBy)
}

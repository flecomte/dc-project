package fr.dcproject.entity

import fr.postgresjson.entity.immutable.*
import java.util.*

open class Opinion<T : TargetI>(
    id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenBasic,
    override val target: T,
    val choice: OpinionChoice
) : ExtraI<T>,
    UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<CitizenBasicI> by EntityCreatedByImp(createdBy) {

    fun getName(): String = choice.name
}

typealias OpinionArticle = Opinion<Article>
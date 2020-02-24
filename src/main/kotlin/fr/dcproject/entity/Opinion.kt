package fr.dcproject.entity

import fr.postgresjson.entity.immutable.EntityCreatedAt
import fr.postgresjson.entity.immutable.EntityCreatedAtImp
import fr.postgresjson.entity.immutable.EntityCreatedBy
import fr.postgresjson.entity.immutable.EntityCreatedByImp
import java.util.*

open class Opinion<T : TargetI>(
    id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenBasic,
    override val target: T,
    val choice: OpinionChoice
) : ExtraI<T,CitizenBasicI>,
    TargetRef(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<CitizenBasicI> by EntityCreatedByImp(createdBy) {

    fun getName(): String = choice.name
}

class OpinionArticle(
    id: UUID = UUID.randomUUID(),
    createdBy: CitizenBasic,
    target: ArticleRef,
    choice: OpinionChoice
) : Opinion<ArticleRef>(id, createdBy, target, choice)
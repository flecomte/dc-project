package fr.dcproject.component.opinion.entity

import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.citizen.CitizenBasic
import fr.dcproject.component.citizen.CitizenBasicI
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.citizen.CitizenRef
import fr.dcproject.common.entity.ExtraI
import fr.dcproject.common.entity.HasTarget
import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.entity.TargetRef
import fr.postgresjson.entity.EntityCreatedAt
import fr.postgresjson.entity.EntityCreatedAtImp
import fr.postgresjson.entity.EntityCreatedBy
import fr.postgresjson.entity.EntityCreatedByImp
import fr.postgresjson.entity.UuidEntityI
import java.util.UUID

@Deprecated("")
open class Opinion<T : TargetI>(
    id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenBasic,
    override val target: T,
    val choice: OpinionChoice
) : OpinionRef(id),
    ExtraI<T, CitizenBasicI>,
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<CitizenBasicI> by EntityCreatedByImp(createdBy) {

    fun getName(): String = choice.name
}

@Deprecated("")
class OpinionArticle(
    id: UUID = UUID.randomUUID(),
    createdBy: CitizenBasic,
    target: ArticleRef,
    choice: OpinionChoice
) : Opinion<ArticleRef>(id, createdBy, target, choice)

data class OpinionForUpdate<T : TargetI>(
    override val id: UUID = UUID.randomUUID(),
    override val target: T,
    val choice: OpinionChoiceRef,
    override val createdBy: CitizenRef
) : OpinionRef(id),
    HasTarget<T>,
    EntityCreatedBy<CitizenI> by EntityCreatedByImp(createdBy)

open class OpinionRef(
    override val id: UUID
) : OpinionI, TargetRef(id)

interface OpinionI : UuidEntityI

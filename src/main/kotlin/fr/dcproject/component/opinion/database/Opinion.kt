package fr.dcproject.component.opinion.database

import fr.dcproject.common.entity.CreatedAt
import fr.dcproject.common.entity.CreatedBy
import fr.dcproject.common.entity.EntityI
import fr.dcproject.common.entity.ExtraI
import fr.dcproject.common.entity.HasTarget
import fr.dcproject.common.entity.TargetI
import fr.dcproject.common.entity.TargetRef
import fr.dcproject.component.citizen.database.CitizenCreator
import fr.dcproject.component.citizen.database.CitizenCreatorI
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.citizen.database.CitizenRef
import java.util.UUID

data class Opinion<T : TargetI>(
    override val id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenCreator,
    override val target: T,
    val choice: OpinionChoice
) : OpinionRef(id),
    ExtraI<T, CitizenCreatorI>,
    CreatedAt by CreatedAt.Imp(),
    CreatedBy<CitizenCreatorI> by CreatedBy.Imp(createdBy) {

    fun getName(): String = choice.name
}

data class OpinionForUpdate<T : TargetI>(
    override val id: UUID = UUID.randomUUID(),
    override val target: T,
    val choice: OpinionChoiceRef,
    override val createdBy: CitizenRef
) : OpinionRef(id),
    HasTarget<T>,
    CreatedBy<CitizenI> by CreatedBy.Imp(createdBy)

open class OpinionRef(
    override val id: UUID
) : OpinionI, TargetRef(id)

sealed interface OpinionI : EntityI

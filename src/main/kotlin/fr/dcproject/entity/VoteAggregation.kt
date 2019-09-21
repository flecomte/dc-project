package fr.dcproject.entity

import fr.postgresjson.entity.EntityI
import fr.postgresjson.entity.EntityUpdatedAt
import fr.postgresjson.entity.EntityUpdatedAtImp

open class VoteAggregation (
    val up: Int,
    val neutral: Int,
    val down: Int
): EntityI,
   EntityUpdatedAt by EntityUpdatedAtImp()
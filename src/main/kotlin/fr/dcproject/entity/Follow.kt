package fr.dcproject.entity
import fr.postgresjson.entity.EntityI
import java.util.*

class Follow <T: EntityI<UUID>> (
    id: UUID = UUID.randomUUID(),
    citizen: Citizen,
    override var target: T
): Extra<T>(id, citizen)

typealias FollowArticleEntity = Follow<Article>
typealias FollowConstitutionEntity = Follow<Constitution>
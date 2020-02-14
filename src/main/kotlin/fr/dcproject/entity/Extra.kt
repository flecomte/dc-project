package fr.dcproject.entity

import fr.postgresjson.entity.immutable.EntityCreatedAt
import fr.postgresjson.entity.immutable.EntityCreatedBy
import fr.postgresjson.entity.immutable.UuidEntity
import fr.postgresjson.entity.immutable.UuidEntityI
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

interface ExtraI<T : TargetI> :
    UuidEntityI,
    EntityCreatedAt,
    EntityCreatedBy<CitizenBasicI> {
    val target: T
}

open class TargetRef(id: UUID = UUID.randomUUID()) : TargetI, UuidEntity(id) {
    override val reference: String = ""
        get() {
            return if (field != "") field else TargetI.getReference(this)
        }
}

interface TargetI : UuidEntityI {
    enum class TargetName(val targetReference: String) {
        Article("article"),
        Constitution("constitution"),
        Comment("comment"),
        Opinion("opinion")
    }

    companion object {
        fun <T : TargetI> getReference(t: KClass<T>): String {
            return when {
                t.isSubclassOf(ArticleRef::class) -> TargetName.Article.targetReference
                t.isSubclassOf(ConstitutionRef::class) -> TargetName.Constitution.targetReference
                t.isSubclassOf(CommentRef::class) -> TargetName.Comment.targetReference
                t.isSubclassOf(Opinion::class) -> TargetName.Opinion.targetReference
                else -> throw error("target not implemented: ${t.qualifiedName}")
            }
        }

        fun getReference(t: TargetI): String {
            val ref = this.getReference(t::class)
            return if (t is ExtraI<*>) {
                "${ref}_on_${t.target.reference}"
            } else {
                ref
            }
        }
    }

    val reference: String
}
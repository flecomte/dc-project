package fr.dcproject.entity

import fr.dcproject.component.article.ArticleRef
import fr.dcproject.component.citizen.CitizenI
import fr.dcproject.component.comment.generic.CommentRef
import fr.postgresjson.entity.EntityCreatedAt
import fr.postgresjson.entity.EntityCreatedBy
import fr.postgresjson.entity.UuidEntity
import fr.postgresjson.entity.UuidEntityI
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

interface ExtraI<T : TargetI, C : CitizenI> :
    UuidEntityI,
    HasTarget<T>,
    EntityCreatedAt,
    EntityCreatedBy<C>

interface HasTarget<T : TargetI> {
    val target: T
}

open class TargetRef(id: UUID? = null, reference: String = "") : TargetI, UuidEntity(id) {

    final override val reference: String
        get() = if (field != "") field else TargetI.getReference(this)

    init {
        this.reference = reference
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
                t.isSubclassOf(OpinionRef::class) -> TargetName.Opinion.targetReference
                else -> throw error("target not implemented: ${t.qualifiedName} \nImplement it or return 'reference' from SQL")
            }
        }

        fun getReference(t: TargetI): String {
            val ref = this.getReference(t::class)
            return if (t is ExtraI<*, *>) {
                "${ref}_on_${t.target.reference}"
            } else {
                ref
            }
        }
    }

    val reference: String
}

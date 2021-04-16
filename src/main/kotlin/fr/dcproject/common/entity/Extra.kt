package fr.dcproject.common.entity

import fr.dcproject.component.article.database.ArticleRef
import fr.dcproject.component.citizen.database.CitizenI
import fr.dcproject.component.citizen.database.CitizenRef
import fr.dcproject.component.comment.generic.database.CommentRef
import fr.dcproject.component.constitution.database.ConstitutionRef
import fr.dcproject.component.opinion.database.OpinionRef
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

interface ExtraI<T : TargetI, C : CitizenI> :
    EntityI,
    HasTarget<T>,
    CreatedAt,
    CreatedBy<C>

interface HasTarget<T : TargetI> {
    val target: T
}

open class TargetRef(id: UUID? = null, reference: String = "") : TargetI, Entity(id) {

    final override val reference: String
        get() = if (field != "") field else TargetI.getReference(this)

    init {
        this.reference = reference
    }
}

interface TargetI : EntityI {
    enum class TargetName(val targetReference: String) {
        Article("article"),
        Constitution("constitution"),
        Comment("comment"),
        Opinion("opinion"),
        Citizen("citizen"),
    }

    companion object {
        fun <T : TargetI> getReference(t: KClass<T>): String {
            return when {
                t.isSubclassOf(ArticleRef::class) -> TargetName.Article.targetReference
                t.isSubclassOf(ConstitutionRef::class) -> TargetName.Constitution.targetReference
                t.isSubclassOf(CommentRef::class) -> TargetName.Comment.targetReference
                t.isSubclassOf(OpinionRef::class) -> TargetName.Opinion.targetReference
                t.isSubclassOf(CitizenRef::class) -> TargetName.Citizen.targetReference
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

package fr.dcproject.entity

import fr.postgresjson.entity.immutable.*
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

interface ExtraI<T : TargetI> :
    UuidEntityI,
    EntityCreatedAt,
    EntityCreatedBy<CitizenBasicI> {
    var target: T
}

abstract class Extra<T : TargetI>(
    id: UUID = UUID.randomUUID(),
    override val createdBy: CitizenBasic,
    override var target: T
) :
    ExtraI<T>,
    UuidEntity(id),
    EntityCreatedAt by EntityCreatedAtImp(),
    EntityCreatedBy<CitizenBasicI> by EntityCreatedByImp(createdBy)

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
        Comment("comment")
    }

    companion object {
        fun <T : TargetI> getReference(t: KClass<T>): String {
            return when {
                t.isSuperclassOf(Article::class) -> TargetName.Article.targetReference
                t.isSuperclassOf(Constitution::class) -> TargetName.Constitution.targetReference
                t.isSuperclassOf(Comment::class) -> TargetName.Comment.targetReference
                else -> throw error("target not implemented")
            }
        }

        fun getReference(t: TargetI): String {
            val ref = this.getReference(t::class)
            return if (t is ExtraI<*>) {
                ref +
                        "_on_" +
                        t.target.reference
            } else {
                ref
            }
        }
    }

    val reference: String
}
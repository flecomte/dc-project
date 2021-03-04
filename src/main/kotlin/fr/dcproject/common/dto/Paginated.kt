package fr.dcproject.common.dto

import fr.dcproject.common.entity.EntityI
import fr.postgresjson.connexion.Paginated

fun <E : EntityI> Paginated<E>.toOutput(setup: (E) -> Any): Any {
    return object {
        val count = this@toOutput.count
        val currentPage = this@toOutput.count
        val limit = this@toOutput.limit
        val offset = this@toOutput.offset
        val total = this@toOutput.total
        val totalPages = this@toOutput.totalPages
        val result = this@toOutput.result.map { setup(it) }
    }
}

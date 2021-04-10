package fr.dcproject.routes

interface PaginatedRequestI {
    val page: Int
    val limit: Int
}

open class PaginatedRequest(
    override val page: Int = 1,
    override val limit: Int = 50
) : PaginatedRequestI

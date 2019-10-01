package fr.dcproject.routes

interface PaginatedRequestI {
    val page: Int
    val limit: Int
}

open class PaginatedRequest(
    page: Int = 1,
    limit: Int = 50
): PaginatedRequestI {
    override val page: Int = if (page < 1) 1 else page
    override val limit: Int = if (limit > 50) 50 else if (limit < 1) 1 else limit
}
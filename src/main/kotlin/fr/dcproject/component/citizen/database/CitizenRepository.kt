package fr.dcproject.component.citizen.database

import fr.dcproject.component.auth.database.UserI
import fr.dcproject.component.citizen.database.CitizenI.Name
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import net.pearx.kasechange.toSnakeCase
import java.util.UUID

class CitizenRepository(override var requester: Requester) : RepositoryI {
    fun findById(id: UUID): Citizen? = requester
        .getFunction("find_citizen_by_id_with_user_and_workgroups")
        .selectOne("id" to id)

    fun findByUser(user: UserI): Citizen? = requester
        .getFunction("find_citizen_by_user_id")
        .selectOne("user_id" to user.id)

    fun findByUsername(username: String): Citizen? = requester
        .getFunction("find_citizen_by_username")
        .selectOne("username" to username)

    fun findByName(name: Name): Citizen? = requester
        .getFunction("find_citizen_by_name")
        .selectOne("name" to name)

    fun findByEmail(email: String): Citizen? = requester
        .getFunction("find_citizen_by_email")
        .selectOne("email" to email)

    fun find(
        page: Int = 1,
        limit: Int = 50,
        sort: String? = null,
        direction: RepositoryI.Direction? = null,
        search: String? = null
    ): Paginated<CitizenCart> = requester
        .getFunction("find_citizens")
        .select(
            page,
            limit,
            "sort" to sort?.toSnakeCase(),
            "direction" to direction,
            "search" to search
        )

    fun upsert(citizen: Citizen): Citizen? = requester
        .getFunction("upsert_citizen")
        .selectOne("resource" to citizen)

    fun insertWithUser(citizen: CitizenForCreate): Citizen? = requester
        .getFunction("insert_citizen_with_user")
        .selectOne("resource" to citizen)
}

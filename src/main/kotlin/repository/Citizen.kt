package fr.dcproject.repository

import fr.dcproject.entity.CitizenBasic
import fr.dcproject.entity.CitizenFull
import fr.dcproject.entity.UserI
import fr.postgresjson.connexion.Paginated
import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import fr.postgresjson.repository.RepositoryI.Direction
import net.pearx.kasechange.toSnakeCase
import java.util.*
import fr.dcproject.entity.Citizen as CitizenEntity

class Citizen(override var requester: Requester) : RepositoryI {
    fun findById(id: UUID, withUser: Boolean = false): CitizenEntity? {
        return requester
            .getFunction(if (withUser) "find_citizen_by_id_with_user" else "find_citizen_by_id")
            .selectOne("id" to id)
    }

    fun findByUser(user: UserI): CitizenEntity? {
        return requester
            .getFunction("find_citizen_by_user_id")
            .selectOne("user_id" to user.id)
    }

    fun findByUsername(unsername: String): CitizenEntity? {
        return requester
            .getFunction("find_citizen_by_username")
            .selectOne("username" to unsername)
    }

    fun findByEmail(email: String): CitizenEntity? {
        return requester
            .getFunction("find_citizen_by_email")
            .selectOne("email" to email)
    }

    fun find(
        page: Int = 1,
        limit: Int = 50,
        sort: String? = null,
        direction: Direction? = null,
        search: String? = null
    ): Paginated<CitizenBasic> {
        return requester
            .getFunction("find_citizens")
            .select(
                page, limit,
                "sort" to sort?.toSnakeCase(),
                "direction" to direction,
                "search" to search
            )
    }

    fun upsert(citizen: CitizenFull): CitizenEntity? {
        return requester
            .getFunction("upsert_citizen")
            .selectOne("resource" to citizen)
    }

    fun insertWithUser(citizen: CitizenFull): CitizenEntity? {
        return requester
            .getFunction("insert_citizen_with_user")
            .selectOne("resource" to citizen)
    }
}

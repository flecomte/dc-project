package fr.dcproject.component.auth.database

import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import io.ktor.auth.UserPasswordCredential
import java.util.UUID

class UserRepository(override var requester: Requester) : RepositoryI {
    fun findByCredentials(credentials: UserPasswordCredential): User? {
        return requester
            .getFunction("check_user")
            .selectOne(
                "username" to credentials.name,
                "password" to credentials.password
            )
    }

    fun findById(id: UUID): User {
        return requester
            .getFunction("find_user_by_id")
            .selectOne(
                "id" to id
            ) ?: throw UserNotFound(id)
    }

    fun insert(user: User): User? {
        return requester
            .getFunction("insert_user")
            .selectOne("resource" to user)
    }

    fun changePassword(user: UserWithPassword) {
        requester
            .getFunction("change_user_password")
            .sendQuery("resource" to user)
    }

    class UserNotFound(override val message: String?, override val cause: Throwable?) : Throwable(message, cause) {
        constructor(id: UUID) : this("No User with ID $id", null)
    }
}

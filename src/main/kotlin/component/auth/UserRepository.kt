package fr.dcproject.component.auth

import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import io.ktor.auth.*
import java.util.*
import fr.dcproject.component.auth.User as UserEntity

class UserRepository(override var requester: Requester) : RepositoryI {
    fun findByCredentials(credentials: UserPasswordCredential): UserEntity? {
        return requester
            .getFunction("check_user")
            .selectOne(
                "username" to credentials.name,
                "plain_password" to credentials.password
            )
    }

    fun findById(id: UUID): UserEntity {
        return requester
            .getFunction("find_user_by_id")
            .selectOne(
                "id" to id
            ) ?: throw UserNotFound(id)
    }

    fun insert(user: UserEntity): UserEntity? {
        return requester
            .getFunction("insert_user")
            .selectOne("resource" to user)
    }

    fun changePassword(user: UserFull) {
        requester
            .getFunction("change_user_password")
            .sendQuery("resource" to user)
    }

    class UserNotFound(override val message: String?, override val cause: Throwable?) : Throwable(message, cause) {
        constructor(id: UUID) : this("No User with ID $id", null)
    }
}

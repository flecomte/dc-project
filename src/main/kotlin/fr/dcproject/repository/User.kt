package fr.dcproject.repository

import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import io.ktor.auth.UserPasswordCredential
import java.util.*
import fr.dcproject.entity.User as UserEntity

class User(override var requester: Requester) : RepositoryI<UserEntity> {
    override val entityName = UserEntity::class

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

    class UserNotFound(override val message: String?, override val cause: Throwable?): Throwable(message, cause) {
        constructor(id: UUID): this("No User with ID $id", null)
    }
}
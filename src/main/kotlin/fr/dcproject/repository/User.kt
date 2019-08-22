package fr.dcproject.repository

import fr.postgresjson.connexion.Requester
import fr.postgresjson.repository.RepositoryI
import io.ktor.auth.UserPasswordCredential
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
}

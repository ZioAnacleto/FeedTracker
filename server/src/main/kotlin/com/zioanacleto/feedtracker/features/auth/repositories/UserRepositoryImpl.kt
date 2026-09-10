package com.zioanacleto.feedtracker.features.auth.repositories

import com.zioanacleto.feedtracker.common.utils.transactionDb
import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.features.auth.models.NewUser
import com.zioanacleto.feedtracker.features.auth.models.StoredUser
import com.zioanacleto.feedtracker.features.auth.models.UserAuthMethodsTable
import com.zioanacleto.feedtracker.features.auth.models.UsersTable
import com.zioanacleto.feedtracker.features.auth.models.parseAuthMethods
import com.zioanacleto.feedtracker.features.auth.models.toUserModel
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class UserRepositoryImpl : UserRepository {

    override suspend fun findByEmail(email: String): StoredUser? = transactionDb {
        val row = UsersTable
            .selectAll()
            .where { UsersTable.email eq email }
            .singleOrNull()
            ?: return@transactionDb null
        StoredUser(
            model = row.toUserModel(authMethodsFor(row[UsersTable.id], row[UsersTable.authMethod])),
            passwordHash = row[UsersTable.passwordHash],
            tokensValidAfter = row[UsersTable.tokensValidAfter],
        )
    }

    override suspend fun findById(userId: String): UserModel? = findStoredById(userId)?.model

    override suspend fun findStoredById(userId: String): StoredUser? = transactionDb {
        val row = UsersTable
            .selectAll()
            .where { UsersTable.id eq userId }
            .singleOrNull()
            ?: return@transactionDb null
        StoredUser(
            model = row.toUserModel(authMethodsFor(userId, row[UsersTable.authMethod])),
            passwordHash = row[UsersTable.passwordHash],
            tokensValidAfter = row[UsersTable.tokensValidAfter],
        )
    }

    override suspend fun create(user: NewUser): UserModel = transactionDb {
        UsersTable.insert {
            it[id] = user.id
            it[email] = user.email
            it[passwordHash] = user.passwordHash
            it[authMethod] = user.authMethod
            it[firstName] = user.firstName
            it[lastName] = user.lastName
            it[createdAt] = user.createdAt
            it[tokensValidAfter] = 0
        }
        insertAuthMethod(user.id, user.authMethod)
        UserModel(
            id = user.id,
            email = user.email,
            authMethods = parseAuthMethods(listOf(user.authMethod), user.authMethod),
            firstName = user.firstName,
            lastName = user.lastName,
        )
    }

    override suspend fun addAuthMethod(userId: String, method: AuthMethod, passwordHash: String?): UserModel = transactionDb {
        val row = UsersTable
            .selectAll()
            .where { UsersTable.id eq userId }
            .single()
        insertAuthMethod(userId, method.name)
        if (passwordHash != null) {
            UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.passwordHash] = passwordHash
            }
        }
        row.toUserModel(authMethodsFor(userId, row[UsersTable.authMethod]))
    }

    override suspend fun updateNames(userId: String, firstName: String, lastName: String): UserModel = transactionDb {
        UsersTable.update({ UsersTable.id eq userId }) {
            it[UsersTable.firstName] = firstName
            it[UsersTable.lastName] = lastName
        }
        val row = UsersTable
            .selectAll()
            .where { UsersTable.id eq userId }
            .single()
        row.toUserModel(authMethodsFor(userId, row[UsersTable.authMethod]))
    }

    override suspend fun updatePassword(userId: String, passwordHash: String, tokensValidAfter: Long): UserModel = transactionDb {
        UsersTable.update({ UsersTable.id eq userId }) {
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.tokensValidAfter] = tokensValidAfter
        }
        insertAuthMethod(userId, AuthMethod.EMAIL.name)
        val row = UsersTable
            .selectAll()
            .where { UsersTable.id eq userId }
            .single()
        row.toUserModel(authMethodsFor(userId, row[UsersTable.authMethod]))
    }

    private fun insertAuthMethod(userId: String, method: String) {
        val alreadyLinked = UserAuthMethodsTable
            .selectAll()
            .where {
                (UserAuthMethodsTable.userId eq userId) and (UserAuthMethodsTable.method eq method)
            }
            .any()
        if (!alreadyLinked) {
            UserAuthMethodsTable.insert {
                it[UserAuthMethodsTable.userId] = userId
                it[UserAuthMethodsTable.method] = method
            }
        }
    }

    private fun authMethodsFor(userId: String, fallback: String): List<AuthMethod> {
        val stored = UserAuthMethodsTable
            .selectAll()
            .where { UserAuthMethodsTable.userId eq userId }
            .map { it[UserAuthMethodsTable.method] }
        return parseAuthMethods(stored, fallback)
    }
}

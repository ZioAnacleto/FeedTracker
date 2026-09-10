package com.zioanacleto.feedtracker.features.auth.repositories

import com.zioanacleto.feedtracker.common.utils.transactionDb
import com.zioanacleto.feedtracker.features.auth.models.EmailVerificationCode
import com.zioanacleto.feedtracker.features.auth.models.EmailVerificationCodesTable
import com.zioanacleto.feedtracker.features.auth.models.toEmailVerificationCode
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.UUID

class EmailVerificationRepositoryImpl : EmailVerificationRepository {

    override suspend fun replaceActiveCode(
        email: String,
        codeHash: String,
        expiresAt: Long,
        createdAt: Long,
        purpose: String,
    ): EmailVerificationCode = transactionDb {
        EmailVerificationCodesTable.deleteWhere {
            (EmailVerificationCodesTable.email eq email) and
                (EmailVerificationCodesTable.purpose eq purpose) and
                (EmailVerificationCodesTable.consumedAt eq null)
        }
        val id = UUID.randomUUID().toString()
        EmailVerificationCodesTable.insert {
            it[EmailVerificationCodesTable.id] = id
            it[EmailVerificationCodesTable.email] = email
            it[EmailVerificationCodesTable.codeHash] = codeHash
            it[EmailVerificationCodesTable.purpose] = purpose
            it[EmailVerificationCodesTable.expiresAt] = expiresAt
            it[EmailVerificationCodesTable.attemptCount] = 0
            it[EmailVerificationCodesTable.consumedAt] = null
            it[EmailVerificationCodesTable.createdAt] = createdAt
        }
        EmailVerificationCode(
            id = id,
            email = email,
            purpose = purpose,
            codeHash = codeHash,
            expiresAt = expiresAt,
            attemptCount = 0,
            consumedAt = null,
            createdAt = createdAt,
        )
    }

    override suspend fun findActiveByEmail(email: String, purpose: String): EmailVerificationCode? = transactionDb {
        EmailVerificationCodesTable
            .selectAll()
            .where {
                (EmailVerificationCodesTable.email eq email) and
                    (EmailVerificationCodesTable.purpose eq purpose) and
                    (EmailVerificationCodesTable.consumedAt eq null)
            }
            .orderBy(EmailVerificationCodesTable.createdAt, SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?.toEmailVerificationCode()
    }

    override suspend fun incrementAttempts(id: String) {
        transactionDb {
            EmailVerificationCodesTable.update({ EmailVerificationCodesTable.id eq id }) {
                with(SqlExpressionBuilder) {
                    it.update(
                        EmailVerificationCodesTable.attemptCount,
                        EmailVerificationCodesTable.attemptCount + 1,
                    )
                }
            }
        }
    }

    override suspend fun consume(id: String, consumedAt: Long) {
        transactionDb {
            EmailVerificationCodesTable.update({ EmailVerificationCodesTable.id eq id }) {
                it[EmailVerificationCodesTable.consumedAt] = consumedAt
            }
        }
    }
}

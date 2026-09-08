package com.zioanacleto.feedtracker.features.auth.repositories

import com.zioanacleto.feedtracker.common.utils.transactionDb
import com.zioanacleto.feedtracker.features.auth.models.RevokedAccessTokensTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class RevokedAccessTokenRepositoryImpl : RevokedAccessTokenRepository {
    override suspend fun revoke(jti: String, expiresAt: Long, revokedAt: Long) {
        transactionDb {
            RevokedAccessTokensTable.deleteWhere { RevokedAccessTokensTable.expiresAt lessEq revokedAt }
            val alreadyRevoked = RevokedAccessTokensTable
                .selectAll()
                .where { RevokedAccessTokensTable.jti eq jti }
                .any()
            if (!alreadyRevoked) {
                RevokedAccessTokensTable.insert {
                    it[RevokedAccessTokensTable.jti] = jti
                    it[RevokedAccessTokensTable.expiresAt] = expiresAt
                    it[RevokedAccessTokensTable.revokedAt] = revokedAt
                }
            }
        }
    }

    override suspend fun isRevoked(jti: String): Boolean = transactionDb {
        RevokedAccessTokensTable
            .selectAll()
            .where { RevokedAccessTokensTable.jti eq jti }
            .any()
    }
}

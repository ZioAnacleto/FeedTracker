package com.zioanacleto.feedtracker.common.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

suspend fun <T> transactionDb(block: Transaction.() -> T) = withContext(Dispatchers.IO) { transaction { block() } }

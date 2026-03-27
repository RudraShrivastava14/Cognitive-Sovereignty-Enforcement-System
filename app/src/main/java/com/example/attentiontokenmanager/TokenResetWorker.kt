package com.example.attentiontokenmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking

class TokenResetWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        return try {
            // Worker.doWork() is NOT a suspend function, so we need runBlocking
            // to call our suspend DAO functions
            runBlocking {
                val database = AppDatabase.getInstance(applicationContext)
                val tokenDao = database.appTokenDao()

                val allTokens = tokenDao.getAllTokens()

                allTokens.forEach { token ->
                    tokenDao.upsertToken(
                        token.copy(
                            remainingTokens = token.maxTokens,
                            lastUpdated = TimeWindow.currentWindowStart()
                        )
                    )
                }
            }

            android.util.Log.d("TokenResetWorker", "Daily token reset done")
            Result.success()

        } catch (e: Exception) {
            android.util.Log.e("TokenResetWorker", "Reset failed", e)
            Result.retry()
        }
    }
}

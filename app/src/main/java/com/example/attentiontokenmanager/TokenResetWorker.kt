package com.example.attentiontokenmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TokenResetWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        return try {
            runBlocking {
                val database = AppDatabase.getInstance(applicationContext)
                val tokenDao = database.appTokenDao()
                val eventDao = database.attentionEventDao()
                val calendarEventDao = database.calendarEventDao()

                val allTokens = tokenDao.getAllTokens()
                val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
                val sinceTimestamp = System.currentTimeMillis() - sevenDaysMs
                val todayWindow = TimeWindow.currentWindowStart()

                // Date formats
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = dateFormat.format(Calendar.getInstance().time)

                // Get calendar events for today
                val todaysEvents = calendarEventDao.getEventsForDate(todayStr)
                val eventNameLower = todaysEvents.joinToString(" ") { it.eventName }.lowercase()

                var isFocusMode = false
                var isLeisureMode = false
                
                val focusKeywords = listOf("exam", "study", "work", "focus", "internship", "job", "test", "school", "coding")
                val leisureKeywords = listOf("party", "chill", "vacation", "break", "holiday", "friend", "game")
                
                if (focusKeywords.any { eventNameLower.contains(it) }) {
                    isFocusMode = true
                }
                if (leisureKeywords.any { eventNameLower.contains(it) }) {
                    isLeisureMode = true
                }

                allTokens.forEach { token ->
                    var newMax = token.maxTokens
                    var logAdjustment: String? = null

                    // 1. Adaptive AI Control Layer
                    if (token.isAdaptiveLearningEnabled) {
                        val stats = eventDao.getAppStatsSince(token.packageName, sinceTimestamp)
                        if (stats.totalEvents >= 10) {
                            val blockRate = stats.blockedEvents.toFloat() / stats.totalEvents.toFloat()

                            if (blockRate >= 0.8f && newMax > 1) {
                                newMax -= 1
                                logAdjustment = "Tokens automatically reduced to $newMax due to historic ${(blockRate * 100).toInt()}% block rate."
                            } else if (blockRate <= 0.2f && newMax < 10) { // Max capacity bounds
                                newMax += 1
                                logAdjustment = "Tokens increased to $newMax due to responsible historic usage."
                            }
                        }
                    }

                    // 2. Calendar Event System Layer (Heuristic Parsing)
                    if (todaysEvents.isNotEmpty()) {
                        if (isFocusMode && !isLeisureMode) {
                            newMax = (newMax / 2).coerceAtLeast(1)
                            if (logAdjustment == null) logAdjustment = "" else logAdjustment += " | "
                            logAdjustment += "Tokens reduced by NLP due to Focus Event scheduled."
                        } else if (isLeisureMode) {
                            newMax = (newMax + 3).coerceAtMost(10)
                            if (logAdjustment == null) logAdjustment = "" else logAdjustment += " | "
                            logAdjustment += "Bonus tokens awarded by NLP for Leisure Event scheduled."
                        }
                    }

                    // Reset and update token parameters
                    tokenDao.upsertToken(
                        token.copy(
                            maxTokens = newMax,
                            remainingTokens = newMax, // Reset daily budget
                            lastUpdated = todayWindow,
                            dailyAdjustmentLog = logAdjustment
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

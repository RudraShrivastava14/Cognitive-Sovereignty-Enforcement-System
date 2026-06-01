package com.example.attentiontokenmanager.analytics

import com.example.attentiontokenmanager.AttentionEventDao
import com.example.attentiontokenmanager.TimeWindow

/**
 * Pure logic class. No Android dependencies except what's passed in.
 * Called from TokenResetWorker at midnight to generate daily insights.
 * Analyzes yesterday's data and produces 3-5 InsightEntity objects.
 */
object InsightGenerator {

    suspend fun generate(
        eventDao: AttentionEventDao,
        yesterdayStart: Long,
        yesterdayEnd: Long,
        todayStart: Long
    ): List<InsightEntity> {

        val insights = mutableListOf<InsightEntity>()

        // ── 1. PEAK DISTRACTION HOUR ──────────────────────────────────────
        try {
            val hourlyData = eventDao.eventsPerHourRaw()
            if (hourlyData.isNotEmpty()) {
                val peak = hourlyData.maxByOrNull { it.count }
                if (peak != null && peak.count > 0) {
                    val hour = peak.hour.toIntOrNull() ?: 0
                    val timeLabel = formatHour(hour)
                    val severity = peak.count
                    val text = when {
                        severity >= 20 -> "You were heavily bombarded at $timeLabel — ${peak.count} notifications hit you at your peak distraction hour."
                        severity >= 10 -> "Peak distraction hour: $timeLabel (${peak.count} notifications). Consider restricting apps during this window."
                        else -> "Most notifications arrived at $timeLabel (${peak.count} total)."
                    }
                    insights.add(
                        InsightEntity(
                            generatedDate = todayStart,
                            insightText = text,
                            insightType = "peak_hour"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("InsightGenerator", "peak_hour failed", e)
        }

        // ── 2. TOP DISTRACTING APP ────────────────────────────────────────
        try {
            val totalBlocked = eventDao.totalBlocked()
            val blockedByApp = eventDao.blockedPerApp()
            if (blockedByApp.isNotEmpty() && totalBlocked > 0) {
                val top = blockedByApp.first()
                val pct = (top.count.toFloat() / totalBlocked * 100).toInt()
                val appLabel = top.packageName.substringAfterLast('.')
                    .replaceFirstChar { it.uppercase() }
                val text = when {
                    pct >= 70 -> "$appLabel is your biggest attention drain — it caused $pct% of all your blocked notifications. Consider cutting its budget further."
                    pct >= 40 -> "$appLabel dominated your blocked list with $pct% of total blocks (${top.count} notifications)."
                    else -> "$appLabel led blocked apps with ${top.count} blocked notifications ($pct% of total)."
                }
                insights.add(
                    InsightEntity(
                        generatedDate = todayStart,
                        insightText = text,
                        insightType = "top_app"
                    )
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("InsightGenerator", "top_app failed", e)
        }

        // ── 3. FOCUS TIME VIOLATIONS ──────────────────────────────────────
        try {
            val violations = eventDao.timeRestrictedCount(yesterdayStart, yesterdayEnd)
            if (violations > 0) {
                val text = when {
                    violations >= 30 -> "Your restricted hours were violated $violations times yesterday. Your time-based rules are working hard — apps tried to reach you $violations times during focus windows."
                    violations >= 10 -> "$violations notifications were blocked during your restricted hours yesterday. Your focus windows are actively protecting you."
                    else -> "$violations notification${if (violations > 1) "s" else ""} arrived during your restricted hours and ${if (violations > 1) "were" else "was"} blocked."
                }
                insights.add(
                    InsightEntity(
                        generatedDate = todayStart,
                        insightText = text,
                        insightType = "focus_violation"
                    )
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("InsightGenerator", "focus_violation failed", e)
        }

        // ── 4. DAILY TREND ────────────────────────────────────────────────
        try {
            val dayBeforeYesterdayStart = yesterdayStart - (24L * 60 * 60 * 1000)
            val yesterdayBlocked = eventDao.blockedBetween(yesterdayStart, yesterdayEnd)
            val dayBeforeBlocked = eventDao.blockedBetween(dayBeforeYesterdayStart, yesterdayStart)

            if (dayBeforeBlocked > 0 && yesterdayBlocked > 0) {
                val change = ((yesterdayBlocked - dayBeforeBlocked).toFloat() / dayBeforeBlocked * 100).toInt()
                val text = when {
                    change >= 30 -> "Distraction level spiked — you blocked $change% more notifications yesterday than the day before. Something disrupted your focus."
                    change <= -30 -> "Strong improvement — you blocked $change% fewer notifications yesterday. Your focus discipline is working."
                    change > 0 -> "Slightly more distractions yesterday — $change% increase in blocked notifications vs the previous day."
                    change < 0 -> "Slightly fewer distractions yesterday — ${-change}% fewer blocked notifications vs the previous day."
                    else -> "Consistent distraction pattern — same block count as the previous day."
                }
                insights.add(
                    InsightEntity(
                        generatedDate = todayStart,
                        insightText = text,
                        insightType = "daily_trend"
                    )
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("InsightGenerator", "daily_trend failed", e)
        }

        // ── 5. STREAK ─────────────────────────────────────────────────────
        try {
            var streakDays = 0
            val oneDayMs = 24L * 60 * 60 * 1000

            // Check last 7 days for under-budget performance
            for (i in 1..7) {
                val dayStart = todayStart - (i * oneDayMs)
                val dayEnd = dayStart + oneDayMs
                val total = eventDao.totalEventsBetween(dayStart, dayEnd)
                val blocked = eventDao.blockedBetween(dayStart, dayEnd)
                if (total > 0) {
                    val blockRate = blocked.toFloat() / total
                    if (blockRate < 0.5f) {
                        streakDays++
                    } else {
                        break // streak broken
                    }
                } else {
                    break // no data = no streak
                }
            }

            if (streakDays >= 2) {
                val text = when {
                    streakDays >= 7 -> "PERFECT WEEK — You stayed under your token budget every single day this week. Your attention is under full control."
                    streakDays >= 5 -> "Impressive streak — $streakDays consecutive days under your token budget. You're building real focus discipline."
                    streakDays >= 3 -> "Good streak — $streakDays days in a row under budget. Keep it going."
                    else -> "You've stayed under your notification budget for $streakDays days in a row."
                }
                insights.add(
                    InsightEntity(
                        generatedDate = todayStart,
                        insightText = text,
                        insightType = "streak"
                    )
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("InsightGenerator", "streak failed", e)
        }

        // Return max 5 insights
        return insights.take(5)
    }

    private fun formatHour(hour: Int): String {
        return when {
            hour == 0 -> "12:00 AM"
            hour < 12 -> "$hour:00 AM"
            hour == 12 -> "12:00 PM"
            else -> "${hour - 12}:00 PM"
        }
    }
}

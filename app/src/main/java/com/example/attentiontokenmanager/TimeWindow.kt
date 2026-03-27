package com.example.attentiontokenmanager



import java.util.Calendar

/**
 * Defines daily attention window
 */
object TimeWindow {

    fun currentWindowStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}


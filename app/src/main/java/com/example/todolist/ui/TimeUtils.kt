package com.example.todolist.ui

import java.util.Calendar

object TimeUtils {
    // 获取今天凌晨 00:00 的时间戳
    fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // 获取今天深夜 23:59 的时间戳
    fun getEndOfToday(): Long = getStartOfToday() + 24 * 60 * 60 * 1000 - 1

    // 获取几天后的深夜时间戳
    fun getEndOfDaysAfter(days: Int): Long {
        return getStartOfToday() + (days.toLong() + 1) * 24 * 60 * 60 * 1000 - 1
    }
}
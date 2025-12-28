package com.example.todolist.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlin.math.max

class TodoReminderScheduler(context: Context) {

    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(todoId: Int, reminderAtMillis: Long, repeatIntervalDays: Int) {
        val triggerAtMillis = max(reminderAtMillis, System.currentTimeMillis() + MIN_DELAY_MS)
        val pendingIntent = buildPendingIntent(todoId)

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager?.canScheduleExactAlarms() == false) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager?.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            }

            else -> alarmManager?.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancel(todoId: Int) {
        alarmManager?.cancel(buildPendingIntent(todoId))
    }

    private fun buildPendingIntent(todoId: Int): PendingIntent {
        val intent = Intent(appContext, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_TODO_ID, todoId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(appContext, todoId, intent, flags)
    }

    companion object {
        private const val MIN_DELAY_MS = 1_000L
    }
}

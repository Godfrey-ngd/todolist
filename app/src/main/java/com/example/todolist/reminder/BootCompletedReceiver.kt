package com.example.todolist.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import com.example.todolist.data.local.AppDatabase

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                restoreReminders(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun restoreReminders(context: Context) {
        val database = AppDatabase.getDatabase(context)
        val todos = database.todoDao().getActiveReminderTodos()
        if (todos.isEmpty()) return

        val scheduler = TodoReminderScheduler(context)
        val now = System.currentTimeMillis()

        todos.forEach { todo ->
            val firstReminderAt = if (todo.reminderAt == 0L) todo.dueDate else todo.reminderAt
            if (firstReminderAt <= 0L) return@forEach

            if (firstReminderAt <= now) {
                TodoNotificationUtils.showReminderNotification(context, todo)

                if (todo.repeatIntervalDays > 0) {
                    val intervalMillis = TimeUnit.DAYS.toMillis(todo.repeatIntervalDays.toLong())
                    var nextReminderAt = firstReminderAt
                    while (nextReminderAt <= now) {
                        nextReminderAt += intervalMillis
                    }
                    scheduler.schedule(todo.id, nextReminderAt, todo.repeatIntervalDays)
                }
            } else {
                scheduler.schedule(todo.id, firstReminderAt, todo.repeatIntervalDays)
            }
        }
    }
}

package com.example.todolist.reminder

import android.content.Context
import com.example.todolist.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object ReminderRestorer {

    suspend fun restore(context: Context, dispatchMissedNotifications: Boolean) {
        withContext(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(context)
            val todos = database.todoDao().getActiveReminderTodos()
            if (todos.isEmpty()) return@withContext

            val scheduler = TodoReminderScheduler(context)
            val now = System.currentTimeMillis()

            todos.forEach { todo ->
                val baselineReminderAt = if (todo.reminderAt == 0L) todo.dueDate else todo.reminderAt
                if (baselineReminderAt <= 0L) return@forEach

                if (baselineReminderAt <= now) {
                    if (dispatchMissedNotifications) {
                        TodoNotificationUtils.showReminderNotification(context, todo)
                    }

                    if (todo.repeatIntervalDays > 0) {
                        val intervalMillis = TimeUnit.DAYS.toMillis(todo.repeatIntervalDays.toLong())
                        var nextReminderAt = baselineReminderAt
                        while (nextReminderAt <= now) {
                            nextReminderAt += intervalMillis
                        }
                        scheduler.schedule(todo.id, nextReminderAt, todo.repeatIntervalDays)
                        database.todoDao().updateTodo(todo.copy(reminderAt = nextReminderAt))
                    }
                } else {
                    if (todo.reminderAt != baselineReminderAt) {
                        database.todoDao().updateTodo(todo.copy(reminderAt = baselineReminderAt))
                    }
                    scheduler.schedule(todo.id, baselineReminderAt, todo.repeatIntervalDays)
                }
            }
        }
    }
}

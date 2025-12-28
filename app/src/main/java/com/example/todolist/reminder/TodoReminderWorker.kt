package com.example.todolist.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todolist.data.local.AppDatabase
import java.util.concurrent.TimeUnit

class TodoReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val todoId = inputData.getInt(KEY_TODO_ID, -1)
        if (todoId == -1) {
            return Result.failure()
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val todo = database.todoDao().getTodoById(todoId) ?: return Result.success()

        if (!todo.reminderEnabled || todo.isCompleted) {
            return Result.success()
        }

        TodoNotificationUtils.showReminderNotification(applicationContext, todo)

        if (todo.repeatIntervalDays > 0) {
            val nextReminderAt = System.currentTimeMillis() +
                TimeUnit.DAYS.toMillis(todo.repeatIntervalDays.toLong())
            TodoReminderScheduler(applicationContext).schedule(
                todoId = todoId,
                reminderAtMillis = nextReminderAt,
                repeatIntervalDays = todo.repeatIntervalDays
            )
        }

        return Result.success()
    }

    companion object {
        const val KEY_TODO_ID = "todo_id"
        const val KEY_REPEAT_INTERVAL_DAYS = "repeat_interval_days"
    }
}

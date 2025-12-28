package com.example.todolist.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import kotlin.math.max

class TodoReminderScheduler(context: Context) {

    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)

    fun schedule(todoId: Int, reminderAtMillis: Long, repeatIntervalDays: Int) {
        val delayMillis = max(reminderAtMillis - System.currentTimeMillis(), 0L)
        val workRequest = OneTimeWorkRequestBuilder<TodoReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    TodoReminderWorker.KEY_TODO_ID to todoId,
                    TodoReminderWorker.KEY_REPEAT_INTERVAL_DAYS to repeatIntervalDays
                )
            )
            .addTag(workTag(todoId))
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName(todoId),
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancel(todoId: Int) {
        workManager.cancelUniqueWork(uniqueWorkName(todoId))
    }

    private fun uniqueWorkName(todoId: Int) = "todo_reminder_$todoId"

    private fun workTag(todoId: Int) = "todo_reminder_tag_$todoId"
}

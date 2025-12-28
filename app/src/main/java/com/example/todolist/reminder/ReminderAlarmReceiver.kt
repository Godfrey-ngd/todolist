package com.example.todolist.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import com.example.todolist.data.local.AppDatabase

class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getIntExtra(EXTRA_TODO_ID, -1)
        if (todoId == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sendReminder(context, todoId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun sendReminder(context: Context, todoId: Int) {
        val database = AppDatabase.getDatabase(context)
        val todo = database.todoDao().getTodoById(todoId) ?: return

        if (!todo.reminderEnabled || todo.isCompleted) return

        TodoNotificationUtils.ensureChannel(context)
        TodoNotificationUtils.showReminderNotification(context, todo)

        if (todo.repeatIntervalDays > 0) {
            val nextReminderAt = System.currentTimeMillis() +
                TimeUnit.DAYS.toMillis(todo.repeatIntervalDays.toLong())
            database.todoDao().updateTodo(todo.copy(reminderAt = nextReminderAt))
            TodoReminderScheduler(context).schedule(todo.id, nextReminderAt, todo.repeatIntervalDays)
        } else {
            database.todoDao().updateTodo(todo.copy(reminderEnabled = false, reminderAt = 0L))
        }
    }

    companion object {
        const val EXTRA_TODO_ID = "extra_todo_id"
    }
}

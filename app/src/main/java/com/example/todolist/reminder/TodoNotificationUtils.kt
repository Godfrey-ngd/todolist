package com.example.todolist.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.example.todolist.MainActivity
import com.example.todolist.R
import com.example.todolist.data.local.TodoEntity

object TodoNotificationUtils {

    private const val CHANNEL_ID = "todo_reminders_channel"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Todo reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminder notifications for your todos"
            }
            manager?.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(context: Context, todo: TodoEntity) {
        ensureChannel(context)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            getPendingIntent(todo.id, flags)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(todo.title)
            .setContentText(todo.description.ifBlank { "Tap to view details" })
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(todo.id, notification)
    }
}

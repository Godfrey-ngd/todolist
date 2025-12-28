package com.example.todolist.ui

import android.app.Application
import com.example.todolist.data.TodoRepository
import com.example.todolist.data.local.AppDatabase
import com.example.todolist.reminder.ReminderRestorer
import com.example.todolist.reminder.TodoNotificationUtils
import com.example.todolist.reminder.TodoReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TodoApplication : Application() {
    // 使用 by lazy 关键字实现懒加载
    // 只有在第一次用到数据库或仓库时，它们才会被初始化
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TodoRepository(database.todoDao()) }
    val reminderScheduler by lazy { TodoReminderScheduler(this) }
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        TodoNotificationUtils.ensureChannel(this)
        applicationScope.launch {
            ReminderRestorer.restore(this@TodoApplication, dispatchMissedNotifications = true)
        }
    }
}
package com.example.todolist.ui

import android.app.Application
import com.example.todolist.data.TodoRepository
import com.example.todolist.data.local.AppDatabase

class TodoApplication : Application() {
    // 使用 by lazy 关键字实现懒加载
    // 只有在第一次用到数据库或仓库时，它们才会被初始化
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TodoRepository(database.todoDao()) }
}
package com.example.todolist.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_table")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",       // 详细信息
    val dueDate: Long,                  // 截止时间（毫秒时间戳）
    val reminderEnabled: Boolean = false,
    val reminderAt: Long = 0L,           // 提醒开始时间（毫秒时间戳）
    val repeatIntervalDays: Int = 1,     // 提醒间隔（天）：1=每天，7=每周，或自定义天数
    val priority: Int = 0,              // 0:低 (绿色), 1:中 (黄色), 2:高 (红色)
    val isCompleted: Boolean = false
)
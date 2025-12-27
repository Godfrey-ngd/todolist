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
    val priority: Int = 0,              // 0:低 (绿色), 1:中 (黄色), 2:高 (红色)
    val isCompleted: Boolean = false
)
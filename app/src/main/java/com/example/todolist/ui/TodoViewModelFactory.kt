package com.example.todolist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todolist.data.TodoRepository
import com.example.todolist.reminder.TodoReminderScheduler

class TodoViewModelFactory(
    private val repository: TodoRepository,
    private val reminderScheduler: TodoReminderScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoViewModel(repository, reminderScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
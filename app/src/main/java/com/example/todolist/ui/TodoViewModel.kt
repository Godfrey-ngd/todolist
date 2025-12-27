package com.example.todolist.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.TodoRepository
import com.example.todolist.data.local.TodoEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    // 1. 用户当前的筛选条件状态（默认显示全部）
    private val _currentFilter = MutableStateFlow(TodoFilter.ALL)
    val currentFilter: StateFlow<TodoFilter> = _currentFilter

    // 2. 经过过滤后的最终数据流
    // 我们将数据库的原始流 (repository.allTodos) 与 筛选条件流 (_currentFilter) 结合
    val filteredTodos: StateFlow<List<TodoEntity>> = repository.allTodos
        .combine(_currentFilter) { todos, filter ->
            val startOfToday = TimeUtils.getStartOfToday()

            when (filter) {
                TodoFilter.ALL -> todos
                TodoFilter.PAST -> todos.filter { it.dueDate < startOfToday }
                TodoFilter.TODAY -> todos.filter { it.dueDate in startOfToday..TimeUtils.getEndOfToday() }
                TodoFilter.NEXT_3_DAYS -> todos.filter { it.dueDate in startOfToday..TimeUtils.getEndOfDaysAfter(3) }
                TodoFilter.NEXT_7_DAYS -> todos.filter { it.dueDate in startOfToday..TimeUtils.getEndOfDaysAfter(7) }
                TodoFilter.NEXT_30_DAYS -> todos.filter { it.dueDate in startOfToday..TimeUtils.getEndOfDaysAfter(30) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. 改变筛选条件的函数
    fun setFilter(filter: TodoFilter) {
        _currentFilter.value = filter
    }

    // 4. 根据紧急程度返回颜色
    fun getPriorityColor(priority: Int): Color {
        return when (priority) {
            2 -> Color(0xFFFF5252) // 高：红色
            1 -> Color(0xFFFFB74D) // 中：橙黄色
            else -> Color(0xFF81C784) // 低：绿色
        }
    }

    // 5. 增强版添加函数
    fun addTodo(title: String, description: String, dueDate: Long, priority: Int) {
        viewModelScope.launch {
            repository.insert(
                TodoEntity(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority
                )
            )
        }
    }

    fun toggleTodo(todo: TodoEntity) = viewModelScope.launch {
        repository.update(todo.copy(isCompleted = !todo.isCompleted))
    }

    fun updateTodo(todo: TodoEntity) = viewModelScope.launch {
        repository.update(todo)
    }

    fun deleteTodo(todo: TodoEntity) = viewModelScope.launch {
        repository.delete(todo)
    }

    suspend fun getTodoById(todoId: Int): TodoEntity? {
        return repository.getTodoById(todoId)
    }
}
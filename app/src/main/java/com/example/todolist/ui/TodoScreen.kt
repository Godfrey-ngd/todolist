package com.example.todolist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.todolist.data.local.TodoEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private fun formatMillisToDateTime(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return formatter.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    viewModel: TodoViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    // 观察过滤后的列表和当前的筛选条件
    val todoList by viewModel.filteredTodos.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("我的记事本") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // 1. 筛选选项卡 (Tabs)
            ScrollableTabRow(
                selectedTabIndex = TodoFilter.entries.indexOf(currentFilter),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                TodoFilter.entries.forEach { filter ->
                    Tab(
                        selected = currentFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        text = { Text(filter.label) }
                    )
                }
            }

            // 2. 列表区域
            LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                items(todoList) { todo ->
                    EnhancedTodoItem(
                        todo = todo,
                        priorityColor = viewModel.getPriorityColor(todo.priority),
                        onClick = { onNavigateToDetail(todo.id) },
                        onCheckedChange = { viewModel.toggleTodo(todo) },
                        onDelete = { viewModel.deleteTodo(todo) }
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedTodoItem(
    todo: TodoEntity,
    priorityColor: Color,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() } // 点击进入详情
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 优先级颜色圆点
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color = priorityColor, shape = CircleShape)
            )

            Checkbox(checked = todo.isCompleted, onCheckedChange = onCheckedChange)

            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
                Text(
                    text = formatMillisToDateTime(todo.dueDate),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )

            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Gray)
            }
        }
    }
}
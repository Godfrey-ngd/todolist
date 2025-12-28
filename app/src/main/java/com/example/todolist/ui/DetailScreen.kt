package com.example.todolist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.todolist.data.local.TodoEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    todoId: Int,
    viewModel: TodoViewModel,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit
) {
    // 1. 定义一个状态来存放查到的待办事项
    var todo by remember { mutableStateOf<TodoEntity?>(null) }

    // 2. 启动时根据 ID 查询数据库
    // 注意：在实际大项目中这里通常在 ViewModel 里处理，为了演示流程我们先这样实现
    LaunchedEffect(todoId) {
        todo = viewModel.getTodoById(todoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("任务详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(todoId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                }
            )
        }
    ) { padding ->
        todo?.let { item ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 紧急程度标签
                val priorityLabel = when(item.priority) {
                    2 -> "紧急"
                    1 -> "中等"
                    else -> "普通"
                }
                Surface(
                    color = viewModel.getPriorityColor(item.priority).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = priorityLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = viewModel.getPriorityColor(item.priority),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // 标题
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineMedium
                )

                // 时间信息
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    val dateStr = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault()).format(Date(item.dueDate))
                    Text(text = "截止日期: $dateStr", style = MaterialTheme.typography.bodyMedium)
                }

                if(item.reminderEnabled){
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        val reminderStr = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault()).format(Date(item.reminderAt))
                        Text(text = "提醒时间: $reminderStr", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if(item.reminderEnabled){
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        val intervalText = when(item.repeatIntervalDays){
                            1 -> "每天"
                            7 -> "每周"
                            else -> "每${item.repeatIntervalDays}天"
                        }
                        Text(text = "重复提醒: $intervalText", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // 详细描述
                Row {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (item.description.isBlank()) "暂无详细描述" else item.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator() // 数据加载中显示转圈
        }
    }
}
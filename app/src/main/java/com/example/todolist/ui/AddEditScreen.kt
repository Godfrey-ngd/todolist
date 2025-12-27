package com.example.todolist.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    viewModel: TodoViewModel,
    onBack: () -> Unit
) {
    // 状态管理
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(0) } // 0:低, 1:中, 2:高
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // 日期选择器对话框状态
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加新任务") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 标题输入
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("任务名称") },
                modifier = Modifier.fillMaxWidth()
            )

            // 2. 详细描述输入
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("详细信息 (可选)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // 3. 优先级选择（紧急程度）
            Text("紧急程度:", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf("低", "中", "高").forEachIndexed { index, label ->
                    RadioButton(
                        selected = (priority == index),
                        onClick = { priority = index }
                    )
                    Text(label)
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            // 4. 日期选择
            val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDate))
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("提醒日期: $dateString")
            }

            Spacer(modifier = Modifier.weight(1f))

            // 5. 保存按钮
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addTodo(title, description, selectedDate, priority)
                        onBack() // 保存后返回主页
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存任务")
            }
        }
    }

    // 日期选择弹窗逻辑
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showDatePicker = false
                }) { Text("确定") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
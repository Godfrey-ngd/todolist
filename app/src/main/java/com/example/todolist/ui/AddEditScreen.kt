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
import java.util.Calendar
import java.util.*


private fun formatMillis(millis: Long, pattern: String): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
}

private fun updateDatePartKeepingTime(currentMillis: Long, newDateMillis: Long): Long {
    val current = Calendar.getInstance().apply { timeInMillis = currentMillis }
    val newDate = Calendar.getInstance().apply { timeInMillis = newDateMillis }
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, newDate.get(Calendar.YEAR))
        set(Calendar.MONTH, newDate.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, newDate.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, current.get(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, current.get(Calendar.MINUTE))
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun updateTimePartKeepingDate(currentMillis: Long, hour: Int, minute: Int): Long {
    return Calendar.getInstance().apply {
        timeInMillis = currentMillis
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    viewModel: TodoViewModel,
    todoId: Int? = null,
    onBack: () -> Unit
) {
    val isEditMode = todoId != null && todoId >= 0

    // 状态管理
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(0) } // 0:低, 1:中, 2:高
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var originalTodo by remember { mutableStateOf<com.example.todolist.data.local.TodoEntity?>(null) }

    // 日期选择器对话框状态
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
    var showDatePicker by remember { mutableStateOf(false) }

    // 时间选择器对话框状态
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().apply { timeInMillis = selectedDate }.get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().apply { timeInMillis = selectedDate }.get(Calendar.MINUTE),
        is24Hour = true
    )
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(todoId) {
        if (isEditMode) {
            val loaded = viewModel.getTodoById(todoId!!)
            if (loaded != null) {
                originalTodo = loaded
                title = loaded.title
                description = loaded.description
                priority = loaded.priority
                selectedDate = loaded.dueDate

                datePickerState.selectedDateMillis = loaded.dueDate
                val cal = Calendar.getInstance().apply { timeInMillis = loaded.dueDate }
                timePickerState.hour = cal.get(Calendar.HOUR_OF_DAY)
                timePickerState.minute = cal.get(Calendar.MINUTE)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "编辑任务" else "添加新任务") },
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
            val dateString = formatMillis(selectedDate, "yyyy-MM-dd")
            val timeString = formatMillis(selectedDate, "HH:mm")

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("提醒日期: $dateString")
            }

            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("提醒时间: $timeString")
            }

            Spacer(modifier = Modifier.weight(1f))

            // 5. 保存按钮
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        if (isEditMode) {
                            val base = originalTodo
                            if (base != null) {
                                viewModel.updateTodo(
                                    base.copy(
                                        title = title,
                                        description = description,
                                        dueDate = selectedDate,
                                        priority = priority
                                    )
                                )
                            }
                        } else {
                            viewModel.addTodo(title, description, selectedDate, priority)
                        }
                        onBack() // 保存后返回主页
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "保存修改" else "保存任务")
            }
        }
    }

    // 日期选择弹窗逻辑
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val pickedDateMillis = datePickerState.selectedDateMillis
                    if (pickedDateMillis != null) {
                        selectedDate = updateDatePartKeepingTime(selectedDate, pickedDateMillis)
                    }
                    showDatePicker = false
                }) { Text("确定") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate = updateTimePartKeepingDate(
                            currentMillis = selectedDate,
                            hour = timePickerState.hour,
                            minute = timePickerState.minute
                        )
                        showTimePicker = false
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            },
            title = { Text("选择时间") },
            text = { TimePicker(state = timePickerState) }
        )
    }
}
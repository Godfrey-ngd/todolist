package com.example.todolist.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
    var dueDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderAtMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var repeatMode by remember { mutableStateOf(ReminderRepeatMode.DAILY) }
    var customRepeatDaysText by remember { mutableStateOf("3") }
    var originalTodo by remember { mutableStateOf<com.example.todolist.data.local.TodoEntity?>(null) }

    // 截止日期选择器状态
    val dueDatePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDateMillis)
    var showDueDatePicker by remember { mutableStateOf(false) }

    val dueTimePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().apply { timeInMillis = dueDateMillis }.get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().apply { timeInMillis = dueDateMillis }.get(Calendar.MINUTE),
        is24Hour = true
    )
    var showDueTimePicker by remember { mutableStateOf(false) }

    // 提醒开始时间选择器状态
    val reminderDatePickerState = rememberDatePickerState(initialSelectedDateMillis = reminderAtMillis)
    var showReminderDatePicker by remember { mutableStateOf(false) }

    val reminderTimePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().apply { timeInMillis = reminderAtMillis }.get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().apply { timeInMillis = reminderAtMillis }.get(Calendar.MINUTE),
        is24Hour = true
    )
    var showReminderTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(todoId) {
        if (isEditMode) {
            val loaded = viewModel.getTodoById(todoId!!)
            if (loaded != null) {
                originalTodo = loaded
                title = loaded.title
                description = loaded.description
                priority = loaded.priority
                dueDateMillis = loaded.dueDate
                reminderEnabled = loaded.reminderEnabled
                reminderAtMillis = if (loaded.reminderAt == 0L) loaded.dueDate else loaded.reminderAt

                repeatMode = when {
                    loaded.repeatIntervalDays == 7 -> ReminderRepeatMode.WEEKLY
                    loaded.repeatIntervalDays == 1 -> ReminderRepeatMode.DAILY
                    else -> {
                        customRepeatDaysText = loaded.repeatIntervalDays.toString()
                        ReminderRepeatMode.CUSTOM
                    }
                }

                dueDatePickerState.selectedDateMillis = dueDateMillis
                val dueCal = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
                dueTimePickerState.hour = dueCal.get(Calendar.HOUR_OF_DAY)
                dueTimePickerState.minute = dueCal.get(Calendar.MINUTE)

                reminderDatePickerState.selectedDateMillis = reminderAtMillis
                val remCal = Calendar.getInstance().apply { timeInMillis = reminderAtMillis }
                reminderTimePickerState.hour = remCal.get(Calendar.HOUR_OF_DAY)
                reminderTimePickerState.minute = remCal.get(Calendar.MINUTE)
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
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState()),
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

            // 4. 截止日期/时间
            val dueDateStr = formatMillis(dueDateMillis, "yyyy-MM-dd")
            val dueTimeStr = formatMillis(dueDateMillis, "HH:mm")

            OutlinedButton(
                onClick = { showDueDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("截止日期: $dueDateStr")
            }

            OutlinedButton(
                onClick = { showDueTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("截止时间: $dueTimeStr")
            }

            // 5. 提醒设置
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("开启提醒", style = MaterialTheme.typography.titleMedium)
                Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
            }

            if (reminderEnabled) {
                val remDateStr = formatMillis(reminderAtMillis, "yyyy-MM-dd")
                val remTimeStr = formatMillis(reminderAtMillis, "HH:mm")

                OutlinedButton(
                    onClick = { showReminderDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("提醒开始日期: $remDateStr")
                }

                OutlinedButton(
                    onClick = { showReminderTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("提醒开始时间: $remTimeStr")
                }

                Text("提醒间隔:", style = MaterialTheme.typography.titleMedium)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = repeatMode == ReminderRepeatMode.DAILY,
                        onClick = { repeatMode = ReminderRepeatMode.DAILY }
                    )
                    Text("每天")
                    Spacer(Modifier.width(12.dp))

                    RadioButton(
                        selected = repeatMode == ReminderRepeatMode.WEEKLY,
                        onClick = { repeatMode = ReminderRepeatMode.WEEKLY }
                    )
                    Text("每周")
                    Spacer(Modifier.width(12.dp))

                    RadioButton(
                        selected = repeatMode == ReminderRepeatMode.CUSTOM,
                        onClick = { repeatMode = ReminderRepeatMode.CUSTOM }
                    )
                    Text("自定义")
                }

                if (repeatMode == ReminderRepeatMode.CUSTOM) {
                    OutlinedTextField(
                        value = customRepeatDaysText,
                        onValueChange = { customRepeatDaysText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("每隔多少天提醒") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 6. 保存按钮
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        if (isEditMode) {
                            val base = originalTodo
                            if (base != null) {
                                val intervalDays = when (repeatMode) {
                                    ReminderRepeatMode.DAILY -> 1
                                    ReminderRepeatMode.WEEKLY -> 7
                                    ReminderRepeatMode.CUSTOM -> customRepeatDaysText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                }
                                viewModel.updateTodo(
                                    base.copy(
                                        title = title,
                                        description = description,
                                        dueDate = dueDateMillis,
                                        priority = priority
                                        ,
                                        reminderEnabled = reminderEnabled,
                                        reminderAt = if (reminderEnabled) reminderAtMillis else 0L,
                                        repeatIntervalDays = intervalDays
                                    )
                                )
                            }
                        } else {
                            val intervalDays = when (repeatMode) {
                                ReminderRepeatMode.DAILY -> 1
                                ReminderRepeatMode.WEEKLY -> 7
                                ReminderRepeatMode.CUSTOM -> customRepeatDaysText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                            }
                            viewModel.addTodo(
                                title = title,
                                description = description,
                                dueDate = dueDateMillis,
                                priority = priority,
                                reminderEnabled = reminderEnabled,
                                reminderAt = if (reminderEnabled) reminderAtMillis else 0L,
                                repeatIntervalDays = intervalDays
                            )
                        }
                        onBack() // 保存后返回主页
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "保存修改" else "保存任务")
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // 截止日期弹窗
    if (showDueDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val pickedDateMillis = dueDatePickerState.selectedDateMillis
                    if (pickedDateMillis != null) {
                        dueDateMillis = updateDatePartKeepingTime(dueDateMillis, pickedDateMillis)
                    }
                    showDueDatePicker = false
                }) { Text("确定") }
            }
        ) {
            DatePicker(state = dueDatePickerState)
        }
    }

    // 截止时间弹窗
    if (showDueTimePicker) {
        AlertDialog(
            onDismissRequest = { showDueTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueDateMillis = updateTimePartKeepingDate(
                            currentMillis = dueDateMillis,
                            hour = dueTimePickerState.hour,
                            minute = dueTimePickerState.minute
                        )
                        showDueTimePicker = false
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDueTimePicker = false }) { Text("取消") }
            },
            title = { Text("选择截止时间") },
            text = { TimePicker(state = dueTimePickerState) }
        )
    }

    // 提醒日期弹窗
    if (showReminderDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showReminderDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val pickedDateMillis = reminderDatePickerState.selectedDateMillis
                    if (pickedDateMillis != null) {
                        reminderAtMillis = updateDatePartKeepingTime(reminderAtMillis, pickedDateMillis)
                    }
                    showReminderDatePicker = false
                }) { Text("确定") }
            }
        ) {
            DatePicker(state = reminderDatePickerState)
        }
    }

    // 提醒时间弹窗
    if (showReminderTimePicker) {
        AlertDialog(
            onDismissRequest = { showReminderTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        reminderAtMillis = updateTimePartKeepingDate(
                            currentMillis = reminderAtMillis,
                            hour = reminderTimePickerState.hour,
                            minute = reminderTimePickerState.minute
                        )
                        showReminderTimePicker = false
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showReminderTimePicker = false }) { Text("取消") }
            },
            title = { Text("选择提醒时间") },
            text = { TimePicker(state = reminderTimePickerState) }
        )
    }
}

private enum class ReminderRepeatMode {
    DAILY,
    WEEKLY,
    CUSTOM
}
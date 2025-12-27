// 文件位置：ui/Screen.kt
package com.example.todolist.ui

sealed class Screen(val route: String) {
    data object List : Screen("todo_list")       // 建议用 data object
    data object Add : Screen("todo_add")

    // 详情页稍微特殊一点，因为它带参数
    data object Detail : Screen("todo_detail/{todoId}") {
        fun createRoute(todoId: Int) = "todo_detail/$todoId"
    }
}
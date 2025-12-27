package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todolist.ui.AddEditScreen
import com.example.todolist.ui.DetailScreen
import com.example.todolist.ui.Screen
import com.example.todolist.ui.TodoApplication
import com.example.todolist.ui.TodoScreen
import com.example.todolist.ui.TodoViewModel
import com.example.todolist.ui.TodoViewModelFactory
import com.example.todolist.ui.theme.TodolistTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as TodoApplication).repository
        val viewModel: TodoViewModel by viewModels { TodoViewModelFactory(repository) }

        setContent {
            TodolistTheme {
                // 在这里调用导航控制器
                TodoAppNavigation(viewModel)
            }
        }
    }
}

@Composable
fun TodoAppNavigation(viewModel: TodoViewModel) {
    val navController = rememberNavController()

    // NavHost 根据“路由”切换不同的屏幕
    NavHost(
        navController = navController,
        startDestination = Screen.List.route
    ) {
        // 1. 列表页路由
        composable(Screen.List.route) {
            TodoScreen(
                viewModel = viewModel,
                onNavigateToAdd = { navController.navigate(Screen.Add.route) },
                onNavigateToDetail = { id ->
                    navController.navigate(Screen.Detail.createRoute(id))
                }
            )
        }

        // 2. 添加页路由
        composable(Screen.Add.route) {
            AddEditScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // 3. 详情页路由
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("todoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val todoId = backStackEntry.arguments?.getInt("todoId") ?: -1
            DetailScreen(
                todoId = todoId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
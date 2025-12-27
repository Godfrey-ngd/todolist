package com.example.todolist

import android.Manifest
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
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

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val app = application as TodoApplication
        val viewModel: TodoViewModel by viewModels {
            TodoViewModelFactory(app.repository, app.reminderScheduler)
        }

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

        // 3. 编辑页路由
        composable(
            route = Screen.Edit.route,
            arguments = listOf(navArgument("todoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val todoId = backStackEntry.arguments?.getInt("todoId") ?: -1
            AddEditScreen(
                viewModel = viewModel,
                todoId = todoId,
                onBack = { navController.popBackStack() }
            )
        }

        // 4. 详情页路由
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("todoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val todoId = backStackEntry.arguments?.getInt("todoId") ?: -1
            DetailScreen(
                todoId = todoId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.Edit.createRoute(id)) }
            )
        }
    }
}
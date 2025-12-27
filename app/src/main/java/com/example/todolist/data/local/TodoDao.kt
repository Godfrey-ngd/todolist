package com.example.todolist.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface TodoDao {
    //获取所有任务，按时间排序
    @Query("SELECT * FROM todo_table ORDER BY dueDate ASC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    //根据ID获取待办事项详情
    @Query("SELECT * FROM todo_table WHERE id = :id")
    suspend fun getTodoById(id: Int): TodoEntity?

    // 插入新任务
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity): Long

    // 更新任务（例如标记完成）
    @Update
    suspend fun updateTodo(todo: TodoEntity)

    // 删除单个任务
    @Delete
    suspend fun deleteTodo(todo: TodoEntity)
}
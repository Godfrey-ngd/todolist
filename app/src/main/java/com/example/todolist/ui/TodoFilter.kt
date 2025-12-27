package com.example.todolist.ui

enum class TodoFilter(val label: String){
    PAST("过去"),
    ALL("全部"),
    TODAY("今天"),
    NEXT_3_DAYS("未来三天"),
    NEXT_7_DAYS("未来七天"),
    NEXT_30_DAYS("未来一个月")
}
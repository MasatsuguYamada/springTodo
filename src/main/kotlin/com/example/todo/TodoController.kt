package com.example.todo

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TodoController {

    @PostMapping("/todo")
    fun addTodoItem(): String {
        return "OK"
    }
}
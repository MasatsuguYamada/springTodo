package com.example.todo

import com.example.todo.repository.TodoRepository
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import java.net.URI
import java.util.*

data class TodoRequest(
    var text: String = ""
)

data class TodoItem(
    @JsonProperty("PK")
    val key: String = "",
    val text: String = ""
)

@RestController
class TodoController (
    val todoRepository: TodoRepository
)
{

    @GetMapping("/todo")
    fun getTodoItem(): List<TodoItem> {
        return todoRepository.getAllItems()
    }

    @GetMapping("/todo/{id}")
    fun getItemById (@PathVariable id: String): ResponseEntity<TodoItem> {
       return ResponseEntity(null, HttpStatus.OK)
    }

    @PostMapping("/todo")
        fun addTodoItem2(@RequestBody req: TodoRequest): String {
            val pk = todoRepository.addNewItem(req)
            return pk
    }

    @PutMapping("/todo/{id}")
    fun updateItem(@PathVariable PK: String,
                   @RequestBody todo: TodoRequest): ResponseEntity<String> {
        return ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @DeleteMapping("/todo/{id}")
    fun deleteItemById (@PathVariable id:String):String {
        return ""
    }
}
package com.example.todo

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

class TodoRequest {
    var text: String = ""
    var name: String = ""
}

data class TodoItem(
    @JsonProperty("PK")
    val key: String = "",
    val text: String = ""
)

@RestController
class TodoController {

    @PostMapping("/todo")
        fun addTodoItem2(@RequestBody req: TodoRequest): String {
            println("req.text=${req.text}")

            val item = mapOf(
                "PK" to AttributeValue.fromS(UUID.randomUUID().toString()),
                "text" to AttributeValue.fromS(req.text)
            )

            val client = DynamoDbClient.builder()
                .endpointOverride(URI.create("http://localhost:4566"))
                .credentialsProvider(AnonymousCredentialsProvider.create())
                .region(Region.AP_NORTHEAST_1)
                .build()

            val putItemRequest = PutItemRequest.builder()
                .tableName("test")
                .item(item)
                .build()
            client.putItem(putItemRequest)

            val scanItemRequest = ScanRequest.builder()
                .tableName("test")
                .build()
            val response = client.scan(scanItemRequest)

            val items = response.items().toList()
            val result = items[0]["PK"]!!.s()

            return result
    }

//    @PostMapping("/todo")
//    fun addTodoItem(@RequestBody req: TodoRequest): String {
//        println("req.text=${req.text}")
//        println("req.name=${req.name}")
//
//        val item = mapOf(
//            "PK" to AttributeValue.fromS("123"),
//            "text" to AttributeValue.fromS(req.text)
//        )
//
//        val client = DynamoDbClient.builder()
//            .endpointOverride(URI.create("http://localhost:4566"))
//            .credentialsProvider(AnonymousCredentialsProvider.create())
//            .region(Region.AP_NORTHEAST_1)
//            .build()
//
//        val putItemRequest = PutItemRequest.builder()
//            .tableName("test")
//            .item(item)
//            .build()
//        client.putItem(putItemRequest)
//
//        val scanItemRequest = ScanRequest.builder()
//            .tableName("test")
//            .build()
//        val response = client.scan(scanItemRequest)
//        val items = response.items().toList()
//        println(items)
//
//        return "OK"
//    }

    @GetMapping("/todo")
    fun getTodoItem(): List<TodoItem> {

        val client = DynamoDbClient.builder()
            .endpointOverride(URI.create("http://localhost:4566"))
            .credentialsProvider(AnonymousCredentialsProvider.create())
            .region(Region.AP_NORTHEAST_1)
            .build()

        val request = ScanRequest.builder()
            .tableName("test")
            .build()

        val responce = client.scan(request)
        val items: List<Map<String, AttributeValue>> = responce.items().toList()

        val resultItems = items.map {
            TodoItem(
                key = it["PK"]!!.s(),//引数が一つの場合はit
                text = it["text"]!!.s()
            )
        }
        return resultItems
    }
    @GetMapping("/todo/{id}")
    fun getItemById (@PathVariable id: String): ResponseEntity<TodoItem> {
        println("-------getItemByID[id]$id")
        val todoItems = getTodoItem()
        val todoItem = todoItems.find { it.key == id }
        if (todoItem != null) {
            return ResponseEntity(todoItem, HttpStatus.OK)
        } else {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @DeleteMapping("/todo/{id}")
    fun deleteItemById (@PathVariable id:String):ResponseEntity<String> {
        println("id*****$id")
        val client = DynamoDbClient.builder()
            .endpointOverride(URI.create("http://localhost:4566"))
            .credentialsProvider(AnonymousCredentialsProvider.create())
            .region(Region.AP_NORTHEAST_1)
            .build()

        val deleteRequest = DeleteItemRequest.builder()
            .tableName("test")
            .key(mapOf("PK" to AttributeValue.builder().s(id).build()))
            .build()
//println("delete start")
//        client.deleteItem(deleteRequest)
//println("delete end")
//
//        return "OK"
        return try {
            client.deleteItem(deleteRequest)
            ResponseEntity("ok$id", HttpStatus.OK)
        } catch (e:Exception) {
            ResponseEntity("ng$id", HttpStatus.NOT_FOUND)
        }
    }
}
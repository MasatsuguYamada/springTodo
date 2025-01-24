package com.example.todo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import java.net.URI
import java.util.*

class TodoRequest {
    var text: String = ""
    var name: String = ""
}

data class TodoItem(
    val PK: String = "",
    val text: String = ""
)

@RestController
class TodoController {

    @PostMapping("/todo2")
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
            println(items)

            return "OK"
    }

    @PostMapping("/todo")
    fun addTodoItem(@RequestBody req: TodoRequest): String {
        println("req.text=${req.text}")
        println("req.name=${req.name}")

        val item = mapOf(
            "PK" to AttributeValue.fromS("123"),
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
        println(items)

        return "OK"
    }

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
                PK = it["PK"]!!.s(),//引数が一つの場合はit
                text = it["text"]!!.s()
            )
        }

//        val resultItems = items.map ( {item ->
//            val todoItem = TodoItem()
//            todoItem.PK = item["PK"]!!.s()
//            todoItem.text = item["text"]!!.s()
//            todoItem
//        })

        return resultItems
    }
}
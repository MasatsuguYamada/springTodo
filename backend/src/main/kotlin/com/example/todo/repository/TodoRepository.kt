package com.example.todo.repository

import com.example.todo.TodoItem
import com.example.todo.TodoRequest
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Repository
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

interface TodoRepository {
    fun getAllItems(): List<TodoItem>
    fun addNewItem(todoRequest: TodoRequest):String
    fun deleteItem(id: String):String
}

@Repository
class DefaultTodoRepository(
    //resources/application.ymlで定義している
    @Value("\${aws.dynamodb-url}") dynamodbURL:String,
    @Value ("\${aws.table-name}") val tableName: String,
): TodoRepository {

    val client: DynamoDbClient

    init {
        client = DynamoDbClient.builder()
            .endpointOverride(URI.create(dynamodbURL))
            .credentialsProvider(AnonymousCredentialsProvider.create())
            .region(Region.AP_NORTHEAST_1)
            .build()

    }

    override fun getAllItems(): List<TodoItem> {
        val items = client.scan(
            ScanRequest.builder()
                .tableName(tableName)
                .build()
        ).items()

        return items.map {
            TodoItem(
                key = it["PK"]!!.s(),
                text = it["text"]!!.s(),
            )
        }
    }

    override fun addNewItem(todoRequest: TodoRequest): String {
        val newPk = UUID.randomUUID()
        client.putItem(
            PutItemRequest.builder()
                .tableName(tableName)
                .item(
                    mapOf(
                        "PK" to AttributeValue.fromS(newPk.toString()),
                        "text" to AttributeValue.fromS(todoRequest.text)
                    )
                )
                .build()
        )
        return newPk.toString()
    }

    override fun deleteItem(id: String): String {
        TODO("Not yet implemented")
    }

}

//@RestController
class TodoRepositoryImpl {

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
            println("$$$$$$$$$$$$")
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

        return try {
            client.deleteItem(deleteRequest)
            ResponseEntity("ok$id", HttpStatus.OK)
        } catch (e:Exception) {
            ResponseEntity("ng$id", HttpStatus.NOT_FOUND)
        }
    }
}
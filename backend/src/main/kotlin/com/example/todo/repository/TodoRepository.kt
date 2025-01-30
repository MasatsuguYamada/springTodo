package com.example.todo.repository

import com.example.todo.TodoItem
import com.example.todo.TodoRequest
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.*
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import java.net.URI
import java.util.*
import com.jayway.jsonpath.JsonPath

interface TodoRepository {
    fun getAllItems(): List<TodoItem>
    fun addNewItem(todoRequest: TodoRequest):String
    fun deleteItem(id: String):String
    fun updateItem(todoItem: TodoItem):String
}

//data class PutItem(
//    val text: String,
//    val pk: String,
//)

@Repository
class DefaultTodoRepository(
    //resources/application.ymlで定義している
    @Value("\${aws.dynamodb-url}") dynamodbURL:String,
    @Value ("\${aws.table-name}") val tableName: String,
): TodoRepository {

    val client: DynamoDbClient

    init {
        println("dynamodbURL**********$dynamodbURL")
        val builder = DynamoDbClient.builder()
            .region(Region.AP_NORTHEAST_1)
        if(dynamodbURL != "default") {
            builder
            .endpointOverride(URI.create(dynamodbURL))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("aaa", "aaa")))

        }
           this.client = builder.build()

    }

    override fun getAllItems(): List<TodoItem> {
        val items = client.scan(
            ScanRequest.builder()
                .tableName(tableName)
                .build()
        ).items()
        println("read override fun $items")
        return items.map {
            TodoItem(
                key = it["PK"]!!.s(),
                text = it["text"]!!.s(),
            )
        }
    }

    override fun addNewItem(todoRequest: TodoRequest): String {
        val newPk = UUID.randomUUID()

        val putRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(
                    mapOf(
                        "PK" to AttributeValue.fromS(newPk.toString()),
                        "text" to AttributeValue.fromS(todoRequest.text)
                    )
                )
                .build()
        client.putItem(putRequest)
        return newPk.toString()
    }

    override fun deleteItem(id: String): String {
        println("override fun DELETE id$id")

        val deleteRequest = DeleteItemRequest.builder()
            .tableName(tableName)
            .key(mapOf("PK" to AttributeValue.builder().s(id).build()))
            .build()
        client.deleteItem(deleteRequest)
        return ""
    }

    override fun updateItem(todoItem: TodoItem): String {
        println("override fun UPDATE")
        val textValue = JsonPath.read<String>(todoItem.text, "$.text")

        val updateRequest = UpdateItemRequest.builder()
            .tableName(tableName)
            .key(mapOf("PK" to AttributeValue.builder().s(todoItem.key).build()))
            .attributeUpdates(
                mapOf(
                    "text" to AttributeValueUpdate.builder()
                        .value(AttributeValue.builder().s(textValue).build())
                        .action(AttributeAction.PUT)
                        .build(),
                )
            )
//            .updateExpression("SET t = :newText")  // 更新する属性とその新しい値
//            .expressionAttributeNames(
//                mapOf(
//                    "#t" to "text" // #textは実際の属性名"text"を指します
//                )
//            )
//            .expressionAttributeValues(
//                mapOf(
//                    "text" to AttributeValue.builder().s(textValue).build()  // 新しいテキストの値を設定
//                )
//            )
            .build()
        client.updateItem(updateRequest)
        return ""
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
println("read impl function")
        val client = DynamoDbClient.builder()
            .endpointOverride(URI.create("http://localhost:4566"))
            .credentialsProvider(AnonymousCredentialsProvider.create())
            .region(Region.AP_NORTHEAST_1)
            .build()

        val request = ScanRequest.builder()
            .tableName("test")
            .build()

        val response = client.scan(request)
        val items: List<Map<String, AttributeValue>> = response.items().toList()

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
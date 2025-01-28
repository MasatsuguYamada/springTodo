package com.example.todo

import com.example.todo.repository.DefaultTodoRepository
import com.example.todo.repository.TodoRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue.fromS
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import java.net.URI
import java.util.*

class TodoRepositoryTest {

    lateinit var todoRepository: TodoRepository
    lateinit var client: DynamoDbClient

    fun deleteAllItems(tableName: String) {
        val request = ScanRequest.builder()
            .tableName(tableName)
            .build()
        val beforeResponse = client.scan(request)
        val beforeItems = beforeResponse.items().toList()
        for (item in beforeItems) {
            val deleteRequest = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(mapOf("PK" to item["PK"])) // .key({ PK: item.PK })
                .build()
            client.deleteItem(deleteRequest)
        }
    }

    @BeforeEach
    fun beforeEach() {
        val url = "http://localhost:4566"
        client = DynamoDbClient.builder()
            .endpointOverride(URI.create(url))
            .credentialsProvider(AnonymousCredentialsProvider.create())
            .region(Region.AP_NORTHEAST_1)
            .build()
        todoRepository= DefaultTodoRepository(
            url,
            "test",
        )
    }

    @AfterEach
    fun afterEach() {
        deleteAllItems("test")
    }

    @Test
    fun getAllItems() {
        val uuid = UUID.randomUUID()
        client.putItem (
            PutItemRequest.builder()
            .tableName("test")
                .item(
                    mapOf(
                        "PK" to fromS(uuid.toString()),
                        "text" to fromS("Hello World!")
                    )
                )
            .build()
        )

        val result = todoRepository.getAllItems()

        assertEquals(
            listOf(
                TodoItem(
                    uuid.toString(),
                    "Hello World!",
                )
            ),
            result
        )
    }

    @Test
    fun addNewItem() {
        val uuid = UUID.fromString("b5b086ae-57e1-4d46-8eea-19471f75b101")
        val mockStatic = Mockito.mockStatic(UUID::class.java)
        mockStatic.`when`<UUID> { UUID.randomUUID() }.thenReturn(uuid)

        val resultPk = todoRepository.addNewItem(
            TodoRequest("Hello World!")
        )

        assertEquals(
            "b5b086ae-57e1-4d46-8eea-19471f75b101",
            resultPk
        )

        val resultResponse = client.getItem(
            GetItemRequest.builder()
                .tableName("test")
                .key(mapOf("PK" to fromS(resultPk)))
                .build()
        )
        assertEquals(
            mapOf(
                "PK" to fromS(resultPk),
                "text" to fromS("Hello World!")
            ),
            resultResponse.item()
        )
        mockStatic.close()
    }

}
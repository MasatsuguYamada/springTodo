package com.example.todo

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import java.net.URI
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
class TodoApplicationTests {

	private val client = DynamoDbClient.builder()
		.endpointOverride(URI.create("http://localhost:4566"))
		.credentialsProvider(AnonymousCredentialsProvider.create())
		.region(Region.AP_NORTHEAST_1)
		.build()

	@Autowired
	private lateinit var mockMvc: MockMvc

	@Test
	fun contextLoads() {
	}

	@Test
	fun `todoエンドポイントにJSONをPOSTすると、200OKが返る`() {
		mockMvc.perform(post("/todo")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"Tsugu\"}"))
			.andExpect(status().isOk)
	}

	@Test
	fun テスト() {
		assertThat(1+2, equalTo(3))
	}

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

	@Test
	fun `todoエンドポイントにfooというJSONをPOSTしてfooがdbにあるか確認`() {

		//Setup
		deleteAllItems("test")

		//Action
		mockMvc.perform(post("/todo")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"text\":\"foo\"}"))
//			.andExpect(status().isOk)

		//Check
		val request = ScanRequest.builder()
			.tableName("test")
			.build()
		val afterResponse = client.scan(request)
		val afterItems = afterResponse.items().toList()
		assertThat(afterItems.size, equalTo(1))
		val firstItem: Map<String, AttributeValue> = afterItems[0]
		val firstItemText: AttributeValue = (firstItem["text"])!!
		val firstItemTextStr: String = firstItemText.s()
		assertThat(firstItemTextStr, equalTo("foo"))
	}

	@Test
	fun `todoエンドポイントにhogeというJSONをPOSTしてhogeがdbにあるか確認`() {

		//Setup
		deleteAllItems("test")

		//Action
		mockMvc.perform(post("/todo")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"text\":\"hoge\"}"))
//			.andExpect(status().isOk)

		//Check
		val request = ScanRequest.builder()
			.tableName("test")
			.build()

		val afterResponse = client.scan(request)
		println("afterResponse.items : ${afterResponse.items()}")
		println("afterResponse.items.toList : ${afterResponse.items().toList()}")

		val afterItems = afterResponse.items().toList()
		assertThat(afterItems.size, equalTo(1))
		val firstItem: Map<String, AttributeValue> = afterItems[0]
		val firstItemText: AttributeValue = (firstItem["text"])!!
		val firstItemTextStr: String = firstItemText.s()
		assertThat(firstItemTextStr, equalTo("hoge"))
		println("result : ${afterItems[0]["text"]!!.s()}")


	}

	@Test
	fun fooとhogeを違うPKでPOSTしてdbに保存を確認() {
		deleteAllItems("test")

		mockMvc.perform(post("/todo2")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"text\":\"foo\"}"))
		mockMvc.perform(post("/todo2")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"text\":\"hoge\"}"))

		val request = ScanRequest.builder()
			.tableName("test")
			.build()
		val afterResponse = client.scan(request)
		val afterItems = afterResponse.items().toList()
		assertThat(afterItems.size, equalTo(2))

	}

	@Test
	fun GETすると現在のDBの項目すべてがリストが返される() {
		deleteAllItems("test")

		val item: Map<String, AttributeValue> = mapOf(
			"PK" to AttributeValue.fromS(UUID.randomUUID().toString()),
			"text" to AttributeValue.fromS("test123")
		)

		val putItemRequest = PutItemRequest.builder()
			.tableName("test")
			.item(item)
			.build()
		client.putItem(putItemRequest)

		mockMvc.perform(get("/todo"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].text").value("test123"))
	}
}

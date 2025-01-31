package com.example.todo

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
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

	@Disabled
	@Test
	fun contextLoads() {
	}

	@Disabled
	@Test
	fun `todoエンドポイントにJSONをPOSTすると、200OKが返る`() {
		mockMvc.perform(post("/todo")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"Tsugu\"}"))
			.andExpect(status().isOk)
	}

	@Disabled
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

	fun putItem(tableName:String, item:Map<String, AttributeValue>) {
		val putItemRequest = PutItemRequest.builder()
			.tableName("test")
			.item(item)
			.build()
		client.putItem(putItemRequest)
	}

	@Disabled
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

	@Disabled
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

	@Disabled
	@Test
	fun fooとhogeを違うPKでPOSTしてdbに保存を確認() {
		deleteAllItems("test")

		mockMvc.perform(post("/todo")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"text\":\"foo\"}"))
		mockMvc.perform(post("/todo")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"text\":\"hoge\"}"))

		val request = ScanRequest.builder()
			.tableName("test")
			.build()
		val afterResponse = client.scan(request)
		val afterItems = afterResponse.items().toList()
		assertThat(afterItems.size, equalTo(2))

	}

	@Disabled
	@Test
	fun GETすると現在のDBの項目すべてがリストが返される() {
		deleteAllItems("test")

		val item: Map<String, AttributeValue> = mapOf(
			"PK" to AttributeValue.fromS(UUID.randomUUID().toString()),
			"text" to AttributeValue.fromS("test456")
		)

		putItem("test", item)
//		val putItemRequest = PutItemRequest.builder()
//			.tableName("test")
//			.item(item)
//			.build()
//		client.putItem(putItemRequest)

		mockMvc.perform(get("/todo"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].text").value("test456"))
	}

	@Disabled
	@Test
	fun `POSTしたときに新しく追加されたPKを返す` () {
		deleteAllItems("test")

		val result = mockMvc.perform(post("/todo")
			.content("{\"text\":\"tsugu\"}")
			.contentType(MediaType.APPLICATION_JSON))
		.andReturn()

		val PK = result.response.contentAsString

		mockMvc.perform(get("/todo"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].PK").value(PK))
	}
	@Disabled
	@Test
	fun `tableを削除`(){
		deleteAllItems("test")
	}

	@Disabled
	@Test
	fun `GET todo {id}をするとその項目だけ返す`() {
		//setup
		deleteAllItems("test")

		val result1 = mockMvc.perform(post("/todo")
			.content("{\"text\":\"tsugutsugu\"}")
			.contentType(MediaType.APPLICATION_JSON))
		.andReturn()
		val PK1 = result1.response.contentAsString
		println("test:[PK1]$PK1")
		val result2 = mockMvc.perform(post("/todo")
			.content("{\"text\":\"sukesuke\"}")
			.contentType(MediaType.APPLICATION_JSON))
			.andReturn()
		val PK2 = result2.response.contentAsString

		mockMvc.perform(get("/todo/$PK1"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.PK").value(PK1))
			.andExpect(jsonPath("$.text").value("tsugutsugu"))
	}

	@Disabled
	@Test
	fun `存在しないidでGETしたら404を返す` (){
		deleteAllItems("test")

		mockMvc.perform(get("/todo/12345"))
			.andExpect(status().isNotFound)
	}

	@Disabled
	@Test
	fun `DELETE todo {id}すると、そのIDを削除する`() {
		deleteAllItems("test")

		val result1 = mockMvc.perform(post("/todo")
			.content("{\"text\":\"tsugutsugu\"}")
			.contentType(MediaType.APPLICATION_JSON))
			.andReturn()
		val PK1 = result1.response.contentAsString
		println("test:[PK1]$PK1")
		val result2 = mockMvc.perform(post("/todo")
			.content("{\"text\":\"sukesuke\"}")
			.contentType(MediaType.APPLICATION_JSON))
			.andReturn()
		val PK2 = result2.response.contentAsString
//		val result3 = mockMvc.perform(post("/todo")
//			.content("{\"text\":\"youyou\"}")
//			.contentType(MediaType.APPLICATION_JSON))
//			.andReturn()
//		val PK3 = result3.response.contentAsString

		val resultDel = mockMvc.perform(delete("/todo/$PK1"))
			.andReturn()
//			.andExpect(status().isOk)
		println("resultDel**$resultDel")
		val contentDel = resultDel.response.contentAsString
		println("resultDel->content**$contentDel")


		val result = mockMvc.perform(get("/todo"))
//			.andExpect(jsonPath("$[0].text").value("tsugu"))
			.andReturn()
		println("result**$result")
		val content = result.response.contentAsString
		println("result->content**$content")
		val mapper = ObjectMapper()
		val items: List<TodoItem> = mapper.readValue(content, object : TypeReference<List<TodoItem>>() {})
//		val items = mapper.readValue<List<TodoItem>>(content)
		println("items : $items")
//		assertThat(items.find {it. == PK1}, equalTo(null))
//		assertThat(items.find {it.id == PK2}, not(equalTo(null)))

//		val result = mockMvc.perform(get("/todo"))
//			.andReturn()
//		val content = result.response.contentAsString
//		val mapper = ObjectMapper()
//		val items = mapper.readValue<List<TodoItem>>(content)
//		assertThat(items.find { it.id == id1 }, equalTo(null))
//		assertThat(items.find { it.id == id2 }, not(equalTo(null)))

	}
}

package com.example.todo

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class TodoApplicationTests {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@Test
	fun contextLoads() {
	}

	@Test
	fun `todoエンドポイントにJSONをPOSTすると、200OKが返る`() {
		mockMvc.perform(post("/todo"))
			.andExpect(status().isOk)
	}

	@Test
	fun テスト() {
		assertThat(1+2, equalTo(3))
	}

	@Test
	fun `todoエンドポイントにfooというJSONをPOSTすると、200OKが返る`() {
		mockMvc.perform(post("/todo").content("{text:\"foo\"}"))
			.andExpect(status().isOk)
	}

}

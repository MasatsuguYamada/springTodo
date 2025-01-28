package com.example.todo

import com.example.todo.repository.TodoRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class TodoControllerTest {

       lateinit var mockTodoRepository: TodoRepository
       lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockTodoRepository = Mockito.mock(TodoRepository::class.java)
        mockMvc = MockMvcBuilders
            .standaloneSetup(TodoController(mockTodoRepository))
            .build()
    }

    @Test
    fun getAllItems() {
        Mockito.`when`(
            mockTodoRepository.getAllItems()
        ).thenReturn(
            listOf(TodoItem("123456789", "Hello World")),
        )

        mockMvc.perform(get("/todo"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].PK").value("123456789"))
            .andExpect(jsonPath("$[0].text").value("Hello World"))

    }

    @Test
    fun addNewItem() {
        Mockito.`when`(
            mockTodoRepository.addNewItem(any())
        ).thenReturn(
            "123456789"
        )

        mockMvc.perform(
            post("/todo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"text":  "Hello World"}
                """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value("123456789"))

        verify(mockTodoRepository, times(1))
            .addNewItem(TodoRequest("Hello World"))
    }

    @Test
    fun deleteItem() {
        Mockito.`when`(
            mockTodoRepository.deleteItem("12345")
        ).thenReturn(
        )
        Mockito.`when`(
            mockTodoRepository.getAllItems()
        ).thenReturn(
            listOf()
        )

        mockMvc.perform(delete("/todo/12345"))
            .andExpect(status().isOk)

        mockMvc.perform(get("/todo"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isEmpty())

        verify(mockTodoRepository,times(1)).deleteItem("12345")
    }
}
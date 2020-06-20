package io.github.bhuwanupadhyay.example

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer.Alphanumeric
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import java.util.function.Consumer

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(Alphanumeric::class)
@ActiveProfiles("test")
internal class IntegrationTest {
    @LocalServerPort
    private val port = 0
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repositories: List<ReactiveCrudRepository<*, *>>

    @BeforeEach
    fun setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
    }

    @AfterEach
    fun tearDown() {
        repositories.forEach(Consumer { obj: ReactiveCrudRepository<*, *> -> obj.deleteAll() })
    }

    @Test
    fun `return 201 if order created successfully`() {
        create().jsonPath("$.id").isNotEmpty
                .jsonPath("$.item").isEqualTo("item")
                .jsonPath("$.quantity").isEqualTo(10)
    }

    @Test
    fun `return 200 if order get by id successfully`() {
        val body = create()
        body.jsonPath("$.id").value { id: Long ->
            client
                    .get()
                    .uri("/orders/$id").exchange().expectStatus().isOk.expectBody()
                    .jsonPath("$.item").isEqualTo("item")
                    .jsonPath("$.quantity").isEqualTo(10)
        }
    }

    @Test
    fun `return 200 if update order successfully`() {
        val body = create()
        body.jsonPath("$.id").value { id: Long ->
            client
                    .put()
                    .uri("/orders/$id").bodyValue(OrderEntity(id, "item-changed", 20)).exchange().expectStatus().isOk.expectBody()
                    .jsonPath("$.item").isEqualTo("item-changed")
                    .jsonPath("$.quantity").isEqualTo(20)
        }
    }

    @Test
    fun `return 200 if order list successfully`() {
        `return 201 if order created successfully`()
        `return 201 if order created successfully`()
        client
                .get()
                .uri("/orders").exchange().expectStatus().isOk.expectBody()
                .jsonPath("$.size()").isEqualTo(2)
    }

    private fun create(): BodyContentSpec {
        return client
                .post()
                .uri("/orders")
                .bodyValue(OrderEntity(-1, "item", 10))
                .exchange()
                .expectStatus()
                .isCreated
                .expectBody()
    }
}

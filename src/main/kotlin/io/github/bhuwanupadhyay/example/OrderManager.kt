package io.github.bhuwanupadhyay.example

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import java.util.concurrent.atomic.AtomicLong

@Table("ORDERS")
data class OrderEntity(@Id var id: Long, var item: String, var quantity: Int)

interface OrderRepository : ReactiveCrudRepository<OrderEntity, Long>

class DomainException(override val message: String, val ex: Throwable?) : RuntimeException(message, ex)

@Component
class OrderHandler(private val repository: OrderRepository) {

    private val sequence: AtomicLong = AtomicLong(1)

    private fun evalId(req: ServerRequest): Long {
        try {
            val id = req.pathVariable("id").toLong()
            if (id > 0) {
                return id
            } else {
                throw DomainException(message = "Id should be positive number.", ex = null)
            }
        } catch (ex: NumberFormatException) {
            throw DomainException(message = "Id should be positive number.", ex = ex)
        }
    }

    fun findAll(req: ServerRequest) = ok().body(repository.findAll())

    fun findOne(req: ServerRequest): Mono<ServerResponse> {
        return ok().body(repository.findById(evalId(req)))
                .switchIfEmpty(notFound().build())
    }

    fun save(req: ServerRequest): Mono<ServerResponse> {
        val payload = req.body(BodyExtractors.toMono(OrderEntity::class.java))
        return payload.flatMap { status(HttpStatus.CREATED).body(repository.save(OrderEntity(sequence.incrementAndGet(), it.item, it.quantity))) }.switchIfEmpty(badRequest().build())
    }

    fun update(req: ServerRequest): Mono<ServerResponse> {
        return repository.existsById(evalId(req))
                .flatMap {
                    val payload = req.body(BodyExtractors.toMono(OrderEntity::class.java))
                    payload.flatMap { ok().body(repository.save(it)) }.switchIfEmpty(badRequest().build())
                }.switchIfEmpty(notFound().build())
    }

}

@Configuration
class OrderRoutes(private val handler: OrderHandler) {

    private val log: Logger = LoggerFactory.getLogger(OrderRoutes::class.java)

    @Bean
    fun router() = router {
        accept(APPLICATION_JSON).nest {
            POST("/orders", handler::save)
            GET("/orders", handler::findAll)
            GET("/orders/{id}", handler::findOne)
            PUT("/orders/{id}", handler::update)
        }
    }.filter { request, next ->
        try {
            next.handle(request)
        } catch (ex: Exception) {
            log.error("Error on rest interface.", ex)
            when (ex) {
                is DomainException -> badRequest().build()
                else -> status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
        }
    }
}

package io.github.bhuwanupadhyay.example

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication
class SpringBoot

fun main(args: Array<String>) {
    runApplication<SpringBoot>(*args)
}

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@EnableR2dbcRepositories
class R2dbcConfiguration {

    @Bean
    fun initializer(connectionFactory: ConnectionFactory, r2dbc: R2dbcProperties): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
        if (r2dbc.url.startsWith("r2dbc:h2")) {
            initializer.setDatabasePopulator(ResourceDatabasePopulator(ClassPathResource("schema-h2.sql")))
        } else if (r2dbc.url.startsWith("r2dbc:postgresql")) {
            initializer.setDatabasePopulator(ResourceDatabasePopulator(ClassPathResource("schema-postgresql.sql")))
        }
        return initializer
    }
}

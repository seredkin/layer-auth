package io.layer.spreadsheet.sharing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.config.WebFluxConfigurer


@SpringBootApplication
@Configuration
class SharingMicroservice {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            bootStrap(args)
        }

        fun bootStrap(args: Array<String>): ConfigurableApplicationContext {
            return runApplication<SharingMicroservice>(*args)
        }
    }

    @Primary
    @Bean
    fun objectMapper() = ObjectMapper().findAndRegisterModules()

    @Bean
    fun webFluxConfig(mapper: ObjectMapper): WebFluxConfigurer {
        return object : WebFluxConfigurer {
            override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
                configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(mapper))
                configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(mapper))
            }
        }
    }
}


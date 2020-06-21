package io.layer.spreadsheet.sharing.component

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@EnableWebFlux
class WebConfiguration : WebFluxConfigurer {

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        val mapper = jacksonObjectMapper().findAndRegisterModules()
        configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(mapper))

        configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(mapper))
    }
}
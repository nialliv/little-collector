package ru.artemev.littlecollector.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {

    @Bean
    fun shadowSlaveWebClient(): RestClient {
        return RestClient.builder()
            .build()
    }

    @Bean
    fun lotmWebClient(): RestClient {
        return RestClient.builder().build()
    }
}
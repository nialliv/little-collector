package ru.artemev.littlecollector.feign

import feign.Headers
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import java.net.URI

@FeignClient(name = "TelegraphClient", url = "https://telegra.ph/")
interface TelegraphClient {

    @Headers("Content-Type: text/html")
    @GetMapping
    fun getHtml(uri: URI): String?

}

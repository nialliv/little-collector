package ru.artemev.littlecollector

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import ru.artemev.littlecollector.dto.InterfaceResponseEnum
import ru.artemev.littlecollector.service.InterfaceService

private val logger = KotlinLogging.logger {}

@SpringBootApplication
class LittleCollectorApplication(
    private val interfaceService: InterfaceService
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        interfaceService.printHello()
        getService(interfaceService.wrapperInput())
    }

    private fun getService(handleResponse: String) {
        when (handleResponse) {
            InterfaceResponseEnum.SHADOW_SLAVE.code -> logger.info { "Это раб" }
            InterfaceResponseEnum.EXIT.code -> logger.info { "А это ты идешь нахер" }
            else -> {
                logger.warn { "Ты ввел ересь, давай по новой" }
                return getService(interfaceService.wrapperInput())
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<LittleCollectorApplication>(*args)
}

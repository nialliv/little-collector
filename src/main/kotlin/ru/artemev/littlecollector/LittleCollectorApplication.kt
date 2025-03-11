package ru.artemev.littlecollector

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import ru.artemev.littlecollector.dto.InterfaceResponseEnum
import ru.artemev.littlecollector.service.InterfaceService
import ru.artemev.littlecollector.service.ShadowSlaveDownloader

private val logger = KotlinLogging.logger {}

@SpringBootApplication
class LittleCollectorApplication(
    private val interfaceService: InterfaceService,
    private val shadowSlaveDownloader: ShadowSlaveDownloader
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        interfaceService.printHello()
        processByService(interfaceService.wrapperInput())
    }

    private fun processByService(handleResponse: String) {
        when (handleResponse) {
            InterfaceResponseEnum.SHADOW_SLAVE.code -> shadowSlaveDownloader.process()
            InterfaceResponseEnum.EXIT.code -> logger.info { "Это ты идешь нахер и пока..." }
            else -> {
                interfaceService.wrongAction()
                return processByService(interfaceService.wrapperInput())
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<LittleCollectorApplication>(*args)
}

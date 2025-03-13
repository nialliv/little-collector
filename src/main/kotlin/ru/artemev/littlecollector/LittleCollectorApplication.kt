package ru.artemev.littlecollector

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import ru.artemev.littlecollector.enums.InterfaceResponseEnum
import ru.artemev.littlecollector.service.InterfaceService
import ru.artemev.littlecollector.service.ShadowSlaveDownloader

private val logger = KotlinLogging.logger {}

@SpringBootApplication
class LittleCollectorApplication(
    private val defaultInterfaceService: InterfaceService,
    private val shadowSlaveDownloader: ShadowSlaveDownloader
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        defaultInterfaceService.printHello()
        defaultInterfaceService.printMenu()
        processByService(defaultInterfaceService.wrapperInput())
    }

    private fun processByService(response: String) {
        when (response) {
            InterfaceResponseEnum.SHADOW_SLAVE.code -> shadowSlaveDownloader.process()
            InterfaceResponseEnum.EXIT.code -> logger.info { "Это ты идешь нахер и пока..." }
            else -> {
                defaultInterfaceService.wrongAction()
                return processByService(defaultInterfaceService.wrapperInput())
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<LittleCollectorApplication>(*args)
}

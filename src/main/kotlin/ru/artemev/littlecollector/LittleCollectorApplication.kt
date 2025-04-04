package ru.artemev.littlecollector

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import ru.artemev.littlecollector.enums.ServicesEnum
import ru.artemev.littlecollector.service.downloaders.LordOfTheMysteriesDownloader
import ru.artemev.littlecollector.service.downloaders.ShadowSlaveDownloader
import ru.artemev.littlecollector.service.printer.PrinterService

private val logger = KotlinLogging.logger {}

@SpringBootApplication
class LittleCollectorApplication(
    private val beginPrinterService: PrinterService,
    //todo create handler maybe
    private val shadowSlaveDownloader: ShadowSlaveDownloader,
    private val lordOfTheMysteriesDownloader: LordOfTheMysteriesDownloader
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        beginPrinterService.printHello()
        beginPrinterService.printMenu()
        processByService(beginPrinterService.wrapperInput())
    }

    private fun processByService(response: String) {
        // todo refactor when
        when (response) {
            ServicesEnum.SHADOW_SLAVE.code -> shadowSlaveDownloader.process()
            ServicesEnum.LORD_OF_THE_MYSTERIES.code -> lordOfTheMysteriesDownloader.process()
            ServicesEnum.EXIT.code -> logger.info { "Это ты идешь нахер и пока..." }
            else -> {
                beginPrinterService.wrongAction()
                return processByService(beginPrinterService.wrapperInput())
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<LittleCollectorApplication>(*args).close()
}

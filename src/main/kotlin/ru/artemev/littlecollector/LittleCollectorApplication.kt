package ru.artemev.littlecollector

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import ru.artemev.littlecollector.enums.ServicesEnum
import ru.artemev.littlecollector.service.downloaders.impl.LotmDownloaderImpl
import ru.artemev.littlecollector.service.downloaders.impl.ShadowSlaveDownloaderImpl
import ru.artemev.littlecollector.utils.PrinterHelper

private val logger = KotlinLogging.logger {}

@EnableFeignClients
@SpringBootApplication
class LittleCollectorApplication(
    private val printerHelper: PrinterHelper,
    private val shadowSlaveDownloader: ShadowSlaveDownloaderImpl,// todo refactor
    private val lordOfTheMysteriesDownloader: LotmDownloaderImpl // todo refactor
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        logger.info { "Привет, чего делать будем?" }
        logger.info {
            "Возможностей пока не так много, выбирай:\n" +
                    "\t${ServicesEnum.SHADOW_SLAVE.code} - скачать немного теневого раба\n" +
                    "\t${ServicesEnum.LORD_OF_THE_MYSTERIES.code} - скачать повелителя тайн\n" +
                    "\t${ServicesEnum.EXIT.code} - сходить нахер и закрыть прогу"
        }
        processByService(printerHelper.wrapperInput())
    }

    private fun processByService(response: String) {
        when (response) {
            ServicesEnum.SHADOW_SLAVE.code -> shadowSlaveDownloader.process()
            ServicesEnum.LORD_OF_THE_MYSTERIES.code -> lordOfTheMysteriesDownloader.process()
            ServicesEnum.EXIT.code -> logger.info { "Это ты идешь нахер и пока..." }
            else -> {
                printerHelper.wrongAction()
                return processByService(printerHelper.wrapperInput())
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<LittleCollectorApplication>(*args).close()
}

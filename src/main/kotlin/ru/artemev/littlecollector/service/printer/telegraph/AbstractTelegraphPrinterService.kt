package ru.artemev.littlecollector.service.printer.telegraph

import io.github.oshai.kotlinlogging.KotlinLogging
import ru.artemev.littlecollector.service.printer.AbstractDefaultPrinterService

private val logger = KotlinLogging.logger { }

abstract class AbstractTelegraphPrinterService : AbstractDefaultPrinterService() {

    //todo тут еще дополнять и дополнять

    fun printInfoAboutCheckLasChapter() {
        logger.info { "Короче, чтоб посмотреть последнюю главу - скачай выгрузку канала" }
    }

    fun askFilePassMessage() {
        logger.info { "Скинь путь до файла выгрузки" }
    }

}
package ru.artemev.littlecollector.service.printer.telegraph

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class LordOfTheMysteriesPrinter
    : AbstractTelegraphPrinterService() {

    override fun printHello() {
        logger.info { "Получается качаем повелителя тайн..." }
    }

    override fun printMenu() {
        logger.info {
            "Что интересует?\n" +
                    "\t1 - Давай качать главы\n"
//                    "\t2 - Давай качать главы"
        }
    }

    override fun printInfoForDownloadChapters() {
        logger.info { "Чтоб скачать всякое - над предварительно выкачать с канала jsonExport" }
    }

    override fun askFilePassMessage() {
        logger.info { "Скинь путь до файла выгрузки" }
    }

    override fun askTargetFolder() {
        logger.info { "В куда сохраняем выгруженные главы?" }
    }

    override fun printProcessChapter(chapterNum: String?) {
        logger.info { "Приступаю к главе - $chapterNum" }
    }

    override fun errorWithChapter(ex: Exception, chapterNum: String?) {
        logger.error { "Проблемка с главой - $chapterNum. Ошибкас - ${ex.message}" }
    }
}
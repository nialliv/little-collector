package ru.artemev.littlecollector.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import ru.artemev.littlecollector.dto.ChapterErrorDto
import ru.artemev.littlecollector.service.ShadowSlaveInterfaceService

private val logger = KotlinLogging.logger {}

@Service
class ShadowSlaveInterfaceImpl
    : AbstractInterfaceServiceImpl(), ShadowSlaveInterfaceService {

    override fun printHello() {
        logger.info { "Получается качаем теневого раба..." }
    }

    override fun printMenu() {
        logger.info {
            "Че качаем?\n" +
                    "\t1 - Какая последняя глава на сайте?\n" +
                    "\t2 - Давай качать главы"
        }
    }

    override fun printInfoForDownloadShadowSlave() {
        logger.info { "Чтоб скачать всякое - над предварительно выкачать с канала jsonExport" }
    }

    override fun askFilePassMessage() {
        logger.info { "Скажи, куда ты положил выгрузку?" }
    }

    override fun askRangeChapters() {
        logger.info { "Какой диапазон глав качаем? Пример ввода: 1-200 ИЛИ 1-2000" }
    }

    override fun askTargetFolder() {
        logger.info { "В куда сохраняем выгруженные главы?" }
    }

    override fun printFinishStatus(chapterWithErrors: HashSet<ChapterErrorDto>) {
        if(chapterWithErrors.isEmpty()) {
            logger.info { "Ну, мы закончили, и походу прошло все без ошибок =)" }
            return
        }
        logger.info { "Ну, мы закончили, и кажись где-то были ошибкасы, так что вот список глав с которыми были проблемы:\n" +
                "\tглавы - $chapterWithErrors" }
    }

    override fun printProcessChapter(chapterNum: Int) {
        logger.info { "Приступаю к главе - $chapterNum" }
    }

}
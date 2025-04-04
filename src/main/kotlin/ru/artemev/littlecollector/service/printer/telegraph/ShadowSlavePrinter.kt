package ru.artemev.littlecollector.service.printer.telegraph

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import ru.artemev.littlecollector.dto.ChapterErrorDto

private val logger = KotlinLogging.logger {  }

@Service
class ShadowSlavePrinter
    : AbstractTelegraphPrinterService() {

    override fun printHello() {
        logger.info { "Получается качаем теневого раба..." }
    }

    override fun printMenu() {
        logger.info {
            "Что интересует?\n" +
                    "\t1 - Какая последняя доступная глава?\n" +
                    "\t2 - Давай качать главы"
        }
    }

    override fun printInfoForDownloadShadowSlave() {
        logger.info { "Чтоб скачать всякое - над предварительно выкачать с канала jsonExport" }
    }



    override fun askRangeChapters() {
        logger.info { "Какой диапазон глав качаем? Пример: 1-200" }
    }

    override fun askTargetFolder() {
        logger.info { "В куда сохраняем выгруженные главы?" }
    }

    override fun printFinishStatus(chapterWithErrors: HashSet<ChapterErrorDto>) {
        if (chapterWithErrors.isEmpty()) {
            logger.info { "Ну, мы закончили, и походу прошло все без ошибок =)" }
            return
        }
        logger.info {
            "Ну, мы закончили, и кажись где-то были ошибкасы, так что вот список глав с которыми были проблемы:\n" +
                    chapterWithErrors.joinToString(",\n")
        }
    }

    override fun printProcessChapter(chapterNum: Int) {
        logger.info { "Приступаю к главе - $chapterNum" }
    }

    override fun errorWithChapter(exception: Exception, chapterNum: Int) {
        logger.error { "Проблемка с главой - $chapterNum. Ошибкас - ${exception.message}" }
    }

    override fun printLastChapter(maxChapter: Int) {
        logger.info { "Итак последняя глава в выгрузке - $maxChapter" }
    }

    override fun askAboutDownloadRange() {
        logger.info { "Зная, какая последняя глава, не хочешь сохранить немного глав? =)" }
    }

}
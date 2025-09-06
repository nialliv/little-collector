package ru.artemev.littlecollector.service.downloaders

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import ru.artemev.littlecollector.dto.ChapterErrorDto
import ru.artemev.littlecollector.dto.ChatExportDto
import ru.artemev.littlecollector.enums.TelegraphActionsEnum
import ru.artemev.littlecollector.utils.Constants.YES
import ru.artemev.littlecollector.utils.PrinterHelper
import ru.artemev.littlecollector.utils.ValidatorHelper
import java.io.File

private val logger = KotlinLogging.logger {}

abstract class AbstractTelegraphDownloader(
    private val printerHelper: PrinterHelper
) : Downloader {

    protected abstract fun getDownloaderName(): String

    protected abstract fun getRangeChapters(chatExport: ChatExportDto): List<Int>

    protected abstract fun getChaptersWithHrefs(chatExport: ChatExportDto): Map<Int, String?>

    protected abstract fun processChapter(
        chapterNum: Int,
        chapterMap: Map<Int, String?>,
        targetFolder: String,
        chapterWithErrors: HashSet<ChapterErrorDto>
    )

    override fun process() {
        logger.info { "Получается качаем ${getDownloaderName()}..." }
        logger.info {
            "Что интересует?\n" +
                    "\t1 - Какой диапазон глав доступен?\n" +
                    "\t2 - Давай качать главы"
        }
        handleActionCode(printerHelper.wrapperInput())
    }

    private fun handleActionCode(wrapperInput: String) {
        when (wrapperInput) {
            TelegraphActionsEnum.CHAPTER_RANGE.actionCode -> getNumberOfLastChapter()
            TelegraphActionsEnum.SAVE_CHAPTERS.actionCode -> saveRangeChapters()
            else -> {
                printerHelper.wrongAction()
                handleActionCode(printerHelper.wrapperInput())
            }
        }
    }

    fun getNumberOfLastChapter() {
        logger.info { "Короче, чтоб посмотреть доступный диапазон - скачай выгрузку канала" }
        val chatExport = getChatExport() ?: return
        val range = getRangeChapters(chatExport)
        logger.info { "Итак диапазон глав в выгрузке - $range" }

        logger.info { "Зная это, не хочешь сохранить немного глав? =)" }
        if (isYesInResponse()) {
            saveRangeChapters(chatExport)
        }
    }

    private fun getChatExport(): ChatExportDto? {
        logger.info { "Скинь путь до файла выгрузки" }
        try {
            return File(printerHelper.wrapperInput())
                .also { answer -> ValidatorHelper.validateFilePath(answer) }
                .let { getChatExportDto(it) }
        } catch (ex: Exception) {
            if (isUserWantAgain(ex)) {
                return getChatExport()
            }
            return null
        }
    }

    fun saveRangeChapters(chatExport: ChatExportDto? = null) {
        logger.info { "Чтоб скачать всякое - над предварительно выкачать с канала jsonExport" }

        val chapterMap = getChaptersWithHrefs(chatExport ?: getChatExport() ?: return)
        val requiredChapters: Set<Int> = getRequireChapters(chapterMap) ?: return

        val targetFolder = getTargetFolder() ?: return

        val chapterWithErrors = HashSet<ChapterErrorDto>()
        requiredChapters.forEach {
            logger.info { "Приступаю к главе - $it" }
            processChapter(it, chapterMap, targetFolder, chapterWithErrors)
        }

        if (chapterWithErrors.isEmpty()) {
            logger.info { "Ну, мы закончили, и походу прошло все без ошибок =)" }
            return
        }
        logger.info {
            "Ну, мы закончили, и кажись где-то были ошибкасы, так что вот список глав с которыми были проблемы:\n" +
                    chapterWithErrors.joinToString(",\n")
        }
    }

    private fun getRequireChapters(chapterMap: Map<Int, String?>): Set<Int>? {
        try {
            logger.info { "Какой диапазон глав качаем? Пример: 1-200" }
            return printerHelper.wrapperInput()
                .also { ValidatorHelper.validateRange(it) }
                .let { convertRangeToSet(it) }
                .also { ValidatorHelper.checkChapterExistsInExport(it, chapterMap) }
        } catch (ex: Exception) {
            if (isUserWantAgain(ex)) {
                return getRequireChapters(chapterMap)
            }
            return null
        }
    }

    private fun getTargetFolder(): String? {
        try {
            logger.info { "В куда сохраняем выгруженные главы?" }
            val targetFolder = printerHelper.wrapperInput()
            ValidatorHelper.validateTargetFolder(targetFolder)
            return targetFolder
        } catch (ex: Exception) {
            if (isUserWantAgain(ex)) {
                return getTargetFolder()
            }
            return null
        }
    }

    private fun convertRangeToSet(chaptersRange: String): Set<Int> {
        return chaptersRange.split("-")
            .let { (it[0].toInt()..it[1].toInt()) }
            .toSet()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun getChatExportDto(filePath: File): ChatExportDto {
        val json = Json {
            ignoreUnknownKeys = true
        }
        return json.decodeFromStream<ChatExportDto>(filePath.inputStream())
    }

    private fun isUserWantAgain(ex: Exception): Boolean {
        printerHelper.error(ex)
        printerHelper.printOtherTry()
        return isYesInResponse()
    }

    private fun isYesInResponse(): Boolean {
        val resp = printerHelper.wrapperYesOrNot()
        return resp.equals(YES, true)
    }


}
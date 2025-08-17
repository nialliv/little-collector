package ru.artemev.littlecollector.service.downloaders.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Service
import ru.artemev.littlecollector.dto.ChapterErrorDto
import ru.artemev.littlecollector.dto.ChatExportDto
import ru.artemev.littlecollector.enums.ServicesEnum
import ru.artemev.littlecollector.enums.TelegraphActionsEnum
import ru.artemev.littlecollector.feign.TelegraphClient
import ru.artemev.littlecollector.utils.Constants.YES
import ru.artemev.littlecollector.utils.PrinterHelper
import ru.artemev.littlecollector.utils.ValidatorHelper
import java.io.File
import java.net.URI

private val logger = KotlinLogging.logger {}

@Service
class ShadowSlaveDownloaderImpl(
    private val printerHelper: PrinterHelper,
    private val telegraphClient: TelegraphClient
) {

    fun isSupported(serviceEnum: ServicesEnum): Boolean {
        return ServicesEnum.SHADOW_SLAVE == serviceEnum
    }

    //todo refactor to abstract class
    fun process() {
        logger.info { "Получается качаем теневого раба..." }
        logger.info {
            "Что интересует?\n" +
                    "\t1 - Какой диапазон глав в выгрузке?\n" +
                    "\t2 - Давай качать главы"
        }
        handleActionCode(printerHelper.wrapperInput())
    }

    //todo refactor to abstract class
    fun handleActionCode(wrapperInput: String) {
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
        logger.info { "Короче, чтоб посмотреть последнюю главу - скачай выгрузку канала" }
        val chatExport = getChatExport() ?: return
        val maxChapter = getMaxChapter(chatExport)
        logger.info { "Итак последняя глава в выгрузке - $maxChapter" }

        logger.info { "Зная, какая последняя глава, не хочешь сохранить немного глав? =)" }
        if (isYesInResponse()) {
            saveRangeChapters(chatExport)
        }
    }

    fun saveRangeChapters(chatExport: ChatExportDto? = null) {
        logger.info { "Чтоб скачать всякое - над предварительно выкачать с канала jsonExport" }

        val chapterMap = getChapters(chatExport ?: getChatExport() ?: return)
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


    //todo refactor this
    private fun processChapter(
        chapterNum: Int,
        chapterMap: Map<Int, String?>,
        targetFolder: String,
        chapterWithErrors: HashSet<ChapterErrorDto>
    ) {
        try {
            // always must be one element, but... mb not? =)
            val href = chapterMap[chapterNum] ?: throw IllegalArgumentException("Href is null")
            val htmlPage = getHtmlPageResponse(href)
            val article = getFirstArticleByJsoup(htmlPage)

            val title = article.getElementsByTag("h1")[0].text()
            val paragraphs = article.getElementsByTag("p")
                .map { it.text() }
                .toMutableList()
                .also { cleaningParagraphs(it) }

            val wordPackage = WordprocessingMLPackage.createPackage()
            val mainDocumentPart = wordPackage.mainDocumentPart
            mainDocumentPart.addStyledParagraphOfText("Title", title)
            mainDocumentPart.addParagraphOfText("")
            paragraphs.forEach { mainDocumentPart.addParagraphOfText(it) }
            wordPackage.save(File("$targetFolder/Глава $chapterNum.docx"))

        } catch (ex: Exception) {
            logger.error { "Проблемка с главой - $chapterNum. Ошибкас - ${ex.message}" }
            chapterWithErrors.add(ChapterErrorDto(chapterNum, ex.message ?: "Message is null =("))
        }
    }

    private fun cleaningParagraphs(paragraphs: MutableList<String>) {
        paragraphs
            .removeAll { it.contains(Regex("^Предыдущая глава$|^Следующая глава$|^$")) }
    }

    private fun getFirstArticleByJsoup(it: String): Element = Jsoup.parse(it).body().getElementsByTag("article")[0]

    private fun getMaxChapter(chatExport: ChatExportDto) = chatExport.messages
        .asSequence()
        .filter { it.type == "message" }
        .flatMap { it.textEntities }
        .filter { it.type == "text_link" }
        .map { getChapter(it.text) }
        .filter { it?.isNotBlank() ?: false }
        .mapNotNull { it?.toInt() }
        .toList()
        .max()

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

    private fun isUserWantAgain(ex: Exception): Boolean {
        printerHelper.error(ex)
        printerHelper.printOtherTry()
        return isYesInResponse()
    }

    private fun isYesInResponse(): Boolean {
        val resp = printerHelper.wrapperYesOrNot()
        return resp.equals(YES, true)
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

    private fun getHtmlPageResponse(href: String): String =
        telegraphClient.getHtml(URI(href))
            ?: throw RuntimeException("Cannot get html by href - $href")


    @OptIn(ExperimentalSerializationApi::class)
    private fun getChatExportDto(filePath: File): ChatExportDto {
        val json = Json {
            ignoreUnknownKeys = true
        }
        return json.decodeFromStream<ChatExportDto>(filePath.inputStream())
    }

    private fun getChapters(chatExport: ChatExportDto): Map<Int, String?> {
        return chatExport.messages
            .filter { it.type == "message" }
            .flatMap { it.textEntities }
            .filter { it.type == "text_link" }
            .associateBy({ getChapter(it.text) }, { it.href })
            .filterKeys { it?.isNotBlank() ?: false }
            .mapKeys { it.key!!.toInt() }
    }

    private fun getChapter(text: String): String? =
        Regex("^.*?\\s(\\d*).*?$").find(text.trim())?.groups?.get(1)?.value

    private fun convertRangeToSet(chaptersRange: String): Set<Int> {
        return chaptersRange.split("-")
            .let { (it[0].toInt()..it[1].toInt()) }
            .toSet()
    }


}
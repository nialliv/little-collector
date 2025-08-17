package ru.artemev.littlecollector.service.downloaders.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.jsoup.Jsoup
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import ru.artemev.littlecollector.dto.ChapterErrorDto
import ru.artemev.littlecollector.dto.ChatExportDto
import ru.artemev.littlecollector.enums.ServicesEnum
import ru.artemev.littlecollector.feign.TelegraphClient
import ru.artemev.littlecollector.utils.Constants.YES
import ru.artemev.littlecollector.utils.PrinterHelper
import ru.artemev.littlecollector.utils.ValidatorHelper
import java.io.File
import java.net.URI

private val logger = KotlinLogging.logger {}

@Service
class LotmDownloaderImpl(
    private val printerHelper: PrinterHelper,
    private val telegraphClient: TelegraphClient
) {

    fun isSupported(serviceEnum: ServicesEnum): Boolean {
        return ServicesEnum.LORD_OF_THE_MYSTERIES == serviceEnum
    }

    fun process() {
        logger.info { "Получается качаем повелителя тайн." }
        logger.info {
            "Что интересует?\n" +
                    "\t1 - Давай качать главы"
        }
        handleActionCode(printerHelper.wrapperInput())
    }

    fun handleActionCode(wrapperInput: String) {
        when (wrapperInput) {
            "1" -> saveRangeChapters()
            else -> {
                printerHelper.wrongAction()
                handleActionCode(printerHelper.wrapperInput())
            }
        }
    }

    private fun saveRangeChapters() {
        logger.info { "Чтоб скачать всякое - над предварительно выкачать с канала jsonExport" }

        val chatExport = getChatExport() ?: return
        val mapChapterToHref = chatExport.messages
            .asSequence()
            .filter { it.type == "message" }
            .filter { message ->
                message.textEntities
                    .any { it.text.contains("повелитель", true) }
            }
            .flatMap { it.textEntities }
            .filter { it.type == "text_link" }
            .filter { !it.text.startsWith("В наши дни") }
            .associateBy({ Regex("^Глава\\s(\\d*).*").find(it.text.trim())?.groups?.get(1)?.value }, { it.href })
            .filterKeys { !it.isNullOrBlank() }

        val targetFolder = getTargetFolder() ?: return

        val chapterWithErrors = HashSet<ChapterErrorDto>()

        mapChapterToHref.forEach { chapterNum ->
            logger.info { "Приступаю к главе - $chapterNum" }
            processChapter(chapterNum, targetFolder, chapterWithErrors)
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

    // todo go to other service
    private fun processChapter(
        chapterToHref: Map.Entry<String?, String?>,
        targetFolder: String,
        chapterWithErrors: java.util.HashSet<ChapterErrorDto>
    ) {
        try {
            // always must be one element, but... mb not? =)
            val href = chapterToHref.value ?: throw IllegalArgumentException("Href is null")
            val htmlPage = getHtmlPageResponse(href)
            val article = Jsoup.parse(htmlPage).body().getElementsByTag("article")[0]

            val title = article.getElementsByTag("h1")[0].text()
            val paragraphs = article.getElementsByTag("p")
                .map { it.text() }
                .toMutableList()

            val wordPackage = WordprocessingMLPackage.createPackage()
            val mainDocumentPart = wordPackage.mainDocumentPart
            mainDocumentPart.addStyledParagraphOfText("Title", title)
            mainDocumentPart.addParagraphOfText("")
            paragraphs.forEach { mainDocumentPart.addParagraphOfText(it) }
            wordPackage.save(File("$targetFolder/Глава ${chapterToHref.key}.docx"))

        } catch (ex: Exception) {
            logger.error { "Проблемка с главой - ${chapterToHref.key}. Ошибкас - ${ex.message}" }
            chapterWithErrors.add(ChapterErrorDto(chapterToHref.key?.toInt() ?: 0, ex.message ?: "Message is null =("))
        }
    }

    private fun getHtmlPageResponse(href: String): String =
        telegraphClient.getHtml(URI(href))
            ?: throw RuntimeException("Cannot get html by href - $href")

    //=============todo refactor to abstract class
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

    //todo mb refactor
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
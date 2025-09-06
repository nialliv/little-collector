package ru.artemev.littlecollector.service.downloaders.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.jsoup.Jsoup
import org.springframework.stereotype.Service
import ru.artemev.littlecollector.dto.ChapterErrorDto
import ru.artemev.littlecollector.dto.ChatExportDto
import ru.artemev.littlecollector.enums.ServicesEnum
import ru.artemev.littlecollector.feign.TelegraphClient
import ru.artemev.littlecollector.service.downloaders.AbstractTelegraphDownloader
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
) : AbstractTelegraphDownloader(printerHelper) {
    override fun isSupported(serviceEnum: ServicesEnum): Boolean {
        return ServicesEnum.LORD_OF_THE_MYSTERIES == serviceEnum
    }

    override fun getDownloaderName(): String = "Lord of the mysteries"

    override fun getRangeChapters(chatExport: ChatExportDto): List<Int> {
        TODO("Not yet implemented")
    }

    override fun getChaptersWithHrefs(chatExport: ChatExportDto): Map<Int, String?> =
        chatExport.messages
            .filter { it.type == "message" }
            .flatMap { it.textEntities }
            .filter { it.type == "text_link" && it.text.contains("повелитель", true) }
            .filter { !it.text.startsWith("В наши дни") }
            .associateBy({ Regex("^Глава\\s(\\d*).*").find(it.text.trim())?.groups?.get(1)?.value }, { it.href })
            .filterKeys { !it.isNullOrBlank() }
            .mapKeys { it.key?.toInt() ?: 0 }

    override fun processChapter(
        chapterNum: Int,
        chapterMap: Map<Int, String?>,
        targetFolder: String,
        chapterWithErrors: HashSet<ChapterErrorDto>
    ) {
        TODO("Not yet implemented")
    }

    // todo go to other service
    private fun processChapter(
        chapterToHref: Map.Entry<String?, String?>,
        targetFolder: String,
        chapterWithErrors: HashSet<ChapterErrorDto>
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
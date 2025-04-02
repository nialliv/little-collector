package ru.artemev.littlecollector.service.downloaders.impl

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import ru.artemev.littlecollector.dto.ChapterErrorDto
import ru.artemev.littlecollector.dto.ChatExportDto
import ru.artemev.littlecollector.enums.LordOfTheMysteriesAction
import ru.artemev.littlecollector.service.downloaders.AbstractTelegraphDownloader
import ru.artemev.littlecollector.service.downloaders.LordOfTheMysteriesDownloader
import ru.artemev.littlecollector.service.interfaces.LordOfTheMysteriesInterfaceService
import ru.artemev.littlecollector.utils.Constants.YES

import ru.artemev.littlecollector.utils.ValidatorHelper
import java.io.File

@Service
class LordOfTheMysteriesDownloaderImpl(
    private val lordOfTheMysteriesInterfaceService: LordOfTheMysteriesInterfaceService,
    @Qualifier("lotmWebClient") private val lotmRestClient: RestClient
) : AbstractTelegraphDownloader(lordOfTheMysteriesInterfaceService), LordOfTheMysteriesDownloader {

    override fun process() {
        lordOfTheMysteriesInterfaceService.printHello()
        lordOfTheMysteriesInterfaceService.printMenu()
        handleActionCode(lordOfTheMysteriesInterfaceService.wrapperInput())
    }

    override fun handleActionCode(wrapperInput: String) {
        when (wrapperInput) {
            LordOfTheMysteriesAction.SAVE_CHAPTERS.actionCode -> saveRangeChapters()
            else -> {
                lordOfTheMysteriesInterfaceService.wrongAction()
                handleActionCode(lordOfTheMysteriesInterfaceService.wrapperInput())
            }
        }
    }

    private fun saveRangeChapters() {
        lordOfTheMysteriesInterfaceService.printInfoForDownloadChapters()

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

        mapChapterToHref.forEach {
            lordOfTheMysteriesInterfaceService.printProcessChapter(it.key)
            processChapter(it, targetFolder, chapterWithErrors)
        }
    }

    private fun processChapter(
        entry: Map.Entry<String?, String?>,
        targetFolder: String,
        chapterWithErrors: java.util.HashSet<ChapterErrorDto>
    ) {
        try {
            // always must be one element, but... mb not? =)
            val href = entry.value ?: throw IllegalArgumentException("Href is null")
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
            wordPackage.save(File("$targetFolder/Глава ${entry.key}.docx"))

        } catch (ex: Exception) {
            lordOfTheMysteriesInterfaceService.errorWithChapter(ex, entry.key)
            chapterWithErrors.add(ChapterErrorDto(entry.key?.toInt() ?: 0, ex.message ?: "Message is null =("))
        }
    }

    private fun getHtmlPageResponse(href: String): String =
        lotmRestClient.get()
            .uri(href)
            .accept(MediaType.TEXT_HTML)
            .retrieve()
            .body<String>()
            ?: throw RuntimeException("Cannot get html by href - $href")

    //=============todo refactor to abstract class
    private fun getTargetFolder(): String? {
        try {
            lordOfTheMysteriesInterfaceService.askTargetFolder()
            val targetFolder = lordOfTheMysteriesInterfaceService.wrapperInput()
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
        lordOfTheMysteriesInterfaceService.askFilePassMessage()
        try {
            return File(lordOfTheMysteriesInterfaceService.wrapperInput())
                .also { ValidatorHelper.validateFilePath(it) }
                .let { getChatExportDto(it) }
        } catch (ex: Exception) {
            if (isUserWantAgain(ex)) {
                return getChatExport()
            }
            return null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun getChatExportDto(filePath: File): ChatExportDto {
        val json = Json {
            ignoreUnknownKeys = true
        }
        return json.decodeFromStream<ChatExportDto>(filePath.inputStream())
    }

    private fun isUserWantAgain(ex: Exception): Boolean {
        lordOfTheMysteriesInterfaceService.error(ex)
        lordOfTheMysteriesInterfaceService.printOtherTry()
        return isYesInResponse()
    }

    private fun isYesInResponse(): Boolean {
        val resp = lordOfTheMysteriesInterfaceService.wrapperYesOrNot()
        return resp.equals(YES, true)
    }
    //=============todo refactor to abstract class
}
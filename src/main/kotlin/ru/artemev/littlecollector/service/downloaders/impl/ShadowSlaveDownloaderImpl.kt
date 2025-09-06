package ru.artemev.littlecollector.service.downloaders.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Service
import ru.artemev.littlecollector.dto.ChapterErrorDto
import ru.artemev.littlecollector.dto.ChatExportDto
import ru.artemev.littlecollector.dto.TextEntity
import ru.artemev.littlecollector.enums.ServicesEnum
import ru.artemev.littlecollector.feign.TelegraphClient
import ru.artemev.littlecollector.service.downloaders.AbstractTelegraphDownloader
import ru.artemev.littlecollector.utils.PrinterHelper
import java.io.File
import java.net.URI

private val logger = KotlinLogging.logger {}

@Service
class ShadowSlaveDownloaderImpl(
    private val printerHelper: PrinterHelper,
    private val telegraphClient: TelegraphClient
) : AbstractTelegraphDownloader(printerHelper) {

    override fun isSupported(serviceEnum: ServicesEnum): Boolean {
        return ServicesEnum.SHADOW_SLAVE == serviceEnum
    }

    override fun getDownloaderName(): String = "shadow slave"

    //todo refactor this
    override fun processChapter(
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

    override fun getChaptersWithHrefs(chatExport: ChatExportDto): Map<Int, String?> {
        return getChaptersOnlyWithHrefs(chatExport)
            .associateBy({ getChapter(it.text) }, { it.href })
    }

    override fun getRangeChapters(chatExport: ChatExportDto): List<Int> {
        val chapters = getChaptersOnlyWithHrefs(chatExport)
            .map { getChapter(it.text) }
            .toList()
        return listOf(chapters.min(), chapters.max())

    }

    private fun getChaptersOnlyWithHrefs(chatExport: ChatExportDto): List<TextEntity> =
        chatExport.messages
            .filter { it.type == "message" }
            .flatMap { it.textEntities }
            .filter { it.type == "text_link" && it.text.contains("глава", ignoreCase = true) }


    private fun getHtmlPageResponse(href: String): String =
        telegraphClient.getHtml(URI(href))
            ?: throw RuntimeException("Cannot get html by href - $href")

    private fun getChapter(text: String): Int =
        text.filter { it.isDigit() }.toInt()

}
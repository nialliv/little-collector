package ru.artemev.littlecollector.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.springframework.stereotype.Service
import ru.artemev.littlecollector.dto.ChapterErrorDto
import ru.artemev.littlecollector.dto.ChatExportDto
import ru.artemev.littlecollector.enums.ShadowSlaveAction
import ru.artemev.littlecollector.service.ShadowSlaveDownloader
import ru.artemev.littlecollector.service.ShadowSlaveInterfaceService
import ru.artemev.littlecollector.utils.ValidatorHelper
import java.io.File

private val logger = KotlinLogging.logger {}

@Service
class ShadowSlaveDownloaderImpl(
    private val shadowSlaveInterfaceService: ShadowSlaveInterfaceService
) : ShadowSlaveDownloader {

    companion object {
        private const val YES = "Y"
    }

    override fun process() {
        shadowSlaveInterfaceService.printHello()
        shadowSlaveInterfaceService.printMenu()
        handleActionCode(shadowSlaveInterfaceService.wrapperInput())
    }

    private fun handleActionCode(wrapperInput: String) {
        when (wrapperInput) {
//            ShadowSlaveAction.LAST_CHAPTER.actionCode -> getNumberOfLastChapter()
            ShadowSlaveAction.SAVE_CHAPTERS.actionCode -> saveRangeChapters()
            else -> {
                shadowSlaveInterfaceService.wrongAction()
                handleActionCode(shadowSlaveInterfaceService.wrapperInput())
            }
        }
    }

    /*
        get info and what needed.
        get path to json export file ++ validate.
        get range to save ++ validate.
        get target folder ++ validate.
        --- try
        download html.
        parse.
        save in doc(?).
        ---- catch
        if error save in array.

        get status.
    */
    override fun saveRangeChapters() {
        shadowSlaveInterfaceService.printInfoForDownloadShadowSlave()

        val chatExport = getChatExport() ?: return
        val chapterMap = getChapters(chatExport)

        val requiredChapters: Set<Int> = getRequireChapters(chapterMap) ?: return

        val targetFolder = getTargetFolder() ?: return

        val chapterWithErrors = HashSet<ChapterErrorDto>()
        requiredChapters.forEach { processChapter(it, chatExport, targetFolder, chapterWithErrors) }

        shadowSlaveInterfaceService.printFinishStatus(chapterWithErrors)
    }

    private fun getTargetFolder(): String? {
        try {
            shadowSlaveInterfaceService.askTargetFolder()
            val targetFolder = shadowSlaveInterfaceService.wrapperInput()
            ValidatorHelper.validateTargetFolder(targetFolder)
            return targetFolder
        } catch (ex: Exception) {
            if(isUserWantAgain(ex)) {
                return getTargetFolder()
            }
            return null
        }
    }

    private fun getRequireChapters(chapterMap: Map<Int?, List<String?>>): Set<Int>? {
        try {
            shadowSlaveInterfaceService.askRangeChapters()
            return shadowSlaveInterfaceService.wrapperInput()
                .also { ValidatorHelper.validateRange(it) }
                .let { convertRangeToSet(it) }
                .also { ValidatorHelper.checkChapterExistsInExport(it, chapterMap) }
        } catch (ex: Exception) {
            if(isUserWantAgain(ex)) {
                return getRequireChapters(chapterMap)
            }
            return null
        }
    }

    private fun getChatExport(): ChatExportDto? {
        shadowSlaveInterfaceService.askFilePassMessage()
        try {
            return File(shadowSlaveInterfaceService.wrapperInput())
                .also { ValidatorHelper.validateFilePath(it) }
                .let { getChatExportDto(it) }
        } catch (ex: Exception) {
            if (isUserWantAgain(ex)) {
                return getChatExport()
            }
            return null
        }
    }

    private fun isUserWantAgain(ex: Exception): Boolean {
        shadowSlaveInterfaceService.error(ex)
        shadowSlaveInterfaceService.printOtherTry()
        val resp = shadowSlaveInterfaceService.wrapperYesOrNot()
        return resp.equals(YES, true)
    }

    private fun processChapter(
        chapterNum: Int,
        chatExport: ChatExportDto,
        targetFolder: String,
        chapterWithErrors: HashSet<ChapterErrorDto>
    ) {
        logger.info { "Типа гружусь с $chapterNum" }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun getChatExportDto(filePath: File): ChatExportDto {
        val json = Json {
            ignoreUnknownKeys = true
        }
        return json.decodeFromStream<ChatExportDto>(filePath.inputStream())
    }

    //todo convert to Map<Int, String>
    private fun getChapters(chatExport: ChatExportDto): Map<Int?, List<String?>> {
        return chatExport.messages
            .filter { it.type == "message" }
            .flatMap { it.textEntities }
            .filter { it.type == "text_link" }
            .groupBy({ getChapter(it.text) }, { it.href })
            .filterKeys { it?.isNotBlank() ?: false }
            .mapKeys { it.key?.toInt() }
    }

    private fun getChapter(text: String): String? =
        Regex("^.*?\\s(\\d*).*?\$").find(text)?.groups?.get(1)?.value

    // 1-23 54-34
    private fun convertRangeToSet(chaptersRange: String): Set<Int> {
        return chaptersRange.split("-")
            .let { (it[0].toInt()..it[1].toInt()) }
            .toSet()
    }


}
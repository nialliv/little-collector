package ru.artemev.littlecollector.service.downloaders

import ru.artemev.littlecollector.dto.ChatExportDto
import ru.artemev.littlecollector.enums.ServicesEnum
import ru.artemev.littlecollector.enums.TelegraphActionsEnum
import ru.artemev.littlecollector.service.printer.PrinterService
import ru.artemev.littlecollector.service.printer.telegraph.AbstractTelegraphPrinterService
import ru.artemev.littlecollector.utils.Constants.YES
import ru.artemev.littlecollector.utils.ValidatorHelper
import java.io.File

abstract class AbstractTelegraphDownloader(
    private val printerService: AbstractTelegraphPrinterService
) : Downloader {

    //todo - пройдись по двум наследникам и найди отличия - общее вынеси сюда
    // к примеру getChapter - у каждого должен быть свой, но getMaxChapter общий мб стоит сделать его абстрактным

    protected abstract fun isSupported(serviceEnum: ServicesEnum): Boolean

    override fun process() {
        printerService.printHello()
        printerService.printMenu()
        handleActionCode(printerService.wrapperInput())
    }

    override fun handleActionCode(wrapperInput: String) {
        when (wrapperInput) {
            TelegraphActionsEnum.LAST_CHAPTER.actionCode -> getNumberOfLastChapter()
            TelegraphActionsEnum.SAVE_CHAPTERS.actionCode -> saveRangeChapters(null)
            else -> {
                printerService.wrongAction()
                handleActionCode(printerService.wrapperInput())
            }
        }
    }

    protected fun getNumberOfLastChapter() {
        printerService.printInfoAboutCheckLasChapter()
        val chatExport = getChatExport() ?: return
        val maxChapter = getMaxChapter(chatExport)
        printerService.printLastChapter(maxChapter)

        printerService.askAboutDownloadRange()
        if (isYesInResponse()) {
            saveRangeChapters(chatExport)
        }
    }

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
        printerService.askFilePassMessage()
        try {
            return File(printerService.wrapperInput())
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
        printerService.error(ex)
        printerService.printOtherTry()
        return isYesInResponse()
    }

    private fun isYesInResponse(): Boolean {
        val resp = printerService.wrapperYesOrNot()
        return resp.equals(YES, true)
    }

}
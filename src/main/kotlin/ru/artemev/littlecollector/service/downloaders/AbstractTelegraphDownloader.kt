package ru.artemev.littlecollector.service.downloaders

import ru.artemev.littlecollector.enums.TelegraphActionsEnum
import ru.artemev.littlecollector.service.interfaces.InterfaceService

abstract class AbstractTelegraphDownloader(
    private val interfaceService: InterfaceService
) : Downloader {

    protected abstract fun isSupported(): Boolean

    override fun process() {
        interfaceService.printHello()
        interfaceService.printMenu()
        handleActionCode(interfaceService.wrapperInput())
    }

    override fun handleActionCode(wrapperInput: String) {
        when (wrapperInput) {
            TelegraphActionsEnum.LAST_CHAPTER.actionCode -> getNumberOfLastChapter()
            TelegraphActionsEnum.SAVE_CHAPTERS.actionCode -> saveRangeChapters(null)
            else -> {
                interfaceService.wrongAction()
                handleActionCode(interfaceService.wrapperInput())
            }
        }
    }

    protected fun getNumberOfLastChapter() {
        interfaceService.printInfoAboutCheckLasChapter()
        val chatExport = getChatExport() ?: return
        val maxChapter = getMaxChapter(chatExport)
        shadowSlaveInterfaceService.printLastChapter(maxChapter)

        shadowSlaveInterfaceService.askAboutDownloadRange()
        if (isYesInResponse()) {
            saveRangeChapters(chatExport)
        }
    }

}
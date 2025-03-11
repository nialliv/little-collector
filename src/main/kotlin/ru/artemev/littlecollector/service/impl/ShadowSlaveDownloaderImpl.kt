package ru.artemev.littlecollector.service.impl

import org.springframework.stereotype.Service
import ru.artemev.littlecollector.dto.ShadowSlaveAction
import ru.artemev.littlecollector.service.InterfaceService
import ru.artemev.littlecollector.service.ShadowSlaveDownloader

@Service
class ShadowSlaveDownloaderImpl(
    private val interfaceService: InterfaceService
) : ShadowSlaveDownloader {

    override fun process() {
        interfaceService.printShadowSlaveHello()
        interfaceService.shadowSlaveMenu()
        handleActionCode(interfaceService.wrapperInput())
    }

    override fun getNumberOfLastChapter(): Int {
        TODO("Not yet implemented")
    }

    override fun saveRangeChapters() {
        TODO("Not yet implemented")
    }

    private fun handleActionCode(wrapperInput: String) {
        when(wrapperInput) {
            ShadowSlaveAction.LAST_CHAPTER.actionCode -> getNumberOfLastChapter()
            ShadowSlaveAction.SAVE_CHAPTERS.actionCode -> saveRangeChapters()
            else -> {
                interfaceService.wrongAction()
                handleActionCode(interfaceService.wrapperInput())
            }
        }
    }



}
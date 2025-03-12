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

    /*
        get info and what needed
        get path to json export file ++ validate
        get range to save ++ validate
        --- try
        download html
        parse
        save in doc(?)
        ---- catch
        if error save in array

        get status
    */
    override fun saveRangeChapters() {

    }

    private fun handleActionCode(wrapperInput: String) {
        when (wrapperInput) {
//            ShadowSlaveAction.LAST_CHAPTER.actionCode -> getNumberOfLastChapter()
            ShadowSlaveAction.SAVE_CHAPTERS.actionCode -> saveRangeChapters()
            else -> {
                interfaceService.wrongAction()
                handleActionCode(interfaceService.wrapperInput())
            }
        }
    }


}
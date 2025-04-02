package ru.artemev.littlecollector.service.interfaces

import ru.artemev.littlecollector.dto.ChapterErrorDto

interface ShadowSlaveInterfaceService : InterfaceService {

    fun printInfoForDownloadShadowSlave()

    fun askFilePassMessage()

    fun askRangeChapters()

    fun askTargetFolder()

    fun printFinishStatus(chapterWithErrors: HashSet<ChapterErrorDto>)

    fun printProcessChapter(chapterNum: Int)

    fun errorWithChapter(exception: Exception, chapterNum: Int)

    fun printInfoAboutCheckLasChapter() // todo go to abstract class

    fun printLastChapter(maxChapter: Int)

    fun askAboutDownloadRange()
}
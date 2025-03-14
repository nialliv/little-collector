package ru.artemev.littlecollector.service

import ru.artemev.littlecollector.dto.ChapterErrorDto

interface ShadowSlaveInterfaceService : InterfaceService {

    fun printInfoForDownloadShadowSlave()

    fun askFilePassMessage()

    fun askRangeChapters()

    fun askTargetFolder()

    fun printFinishStatus(chapterWithErrors: HashSet<ChapterErrorDto>)

    fun printProcessChapter(chapterNum: Int)

    fun errorWithChapter(exception: Exception, chapterNum: Int)

    fun printInfoAboutCheckLasChapter()

    fun printLastChapter(maxChapter: Int)

    fun askAboutDownloadRange()
}
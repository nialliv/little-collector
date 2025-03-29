package ru.artemev.littlecollector.service.interfaces

interface LordOfTheMysteriesInterfaceService : InterfaceService {

    fun printInfoForDownloadChapters()

    fun askFilePassMessage()

    fun askTargetFolder()

    fun printProcessChapter(chapterNum: String?)

    fun errorWithChapter(ex: Exception, chapterNum: String?)
}
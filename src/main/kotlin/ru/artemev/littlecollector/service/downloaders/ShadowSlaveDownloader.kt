package ru.artemev.littlecollector.service.downloaders

import ru.artemev.littlecollector.dto.ChatExportDto

interface ShadowSlaveDownloader : Downloader {

    // Получение номера последней главы
    fun getNumberOfLastChapter()

    // Сохранение ренджа глав - пример: 1-30, 1790-2000 + вопросы как и куда сохранять
    fun saveRangeChapters(chatExport: ChatExportDto?)
}


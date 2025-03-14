package ru.artemev.littlecollector.service

import ru.artemev.littlecollector.dto.ChatExportDto

interface ShadowSlaveDownloader {

    fun process()

    // Получение номера последней главы
    fun getNumberOfLastChapter()

    // Сохранение ренджа глав - пример: 1-30, 1790-2000 + вопросы как и куда сохранять
    fun saveRangeChapters(chatExport: ChatExportDto?)
}


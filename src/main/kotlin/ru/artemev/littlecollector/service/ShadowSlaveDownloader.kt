package ru.artemev.littlecollector.service

interface ShadowSlaveDownloader {

    fun process()

    //uncomment when created tg bot
    // Получение номера последней главы
//    fun getNumberOfLastChapter(): Int

    // Сохранение ренджа глав - пример: 1-30, 1790-2000 + вопросы как и куда сохранять
    fun saveRangeChapters()
}


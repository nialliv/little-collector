package ru.artemev.littlecollector.service.downloaders

import ru.artemev.littlecollector.enums.ServicesEnum

interface Downloader {

    fun isSupported(serviceEnum: ServicesEnum): Boolean

    fun process()
}
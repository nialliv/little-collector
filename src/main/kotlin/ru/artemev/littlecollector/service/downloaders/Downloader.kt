package ru.artemev.littlecollector.service.downloaders

interface Downloader {

    fun process()

    fun handleActionCode(wrapperInput: String)
}
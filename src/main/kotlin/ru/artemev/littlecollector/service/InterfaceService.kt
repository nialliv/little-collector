package ru.artemev.littlecollector.service

import ru.artemev.littlecollector.dto.InterfaceResponseEnum

interface InterfaceService {

    // Приветственное сообщение
    fun printHello()

    // help?
    fun printHelpMessage()

    fun wrapperInput(): String

}
package ru.artemev.littlecollector.service

interface InterfaceService {

    fun printHello()

    fun printMenu()

    fun printOtherTry()

    fun wrapperInput(): String

    fun wrapperYesOrNot(): String

    fun wrongAction()

    fun error(exception: Exception)

}
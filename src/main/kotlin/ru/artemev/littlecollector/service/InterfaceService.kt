package ru.artemev.littlecollector.service

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

//todo create abstract class
interface InterfaceService {

    companion object {
        const val GREEN = "\u001b[32m"
        const val RED = "\u001b[31m"
        const val RESET = "\u001B[0m"
    }

    fun printHello()

    fun printMenu()

    fun printOtherTry() {
        logger.info { "Пробанем еще разок?" }
    }

    fun wrapperInput(): String {
        print("[${GREEN}Input$RESET] -> ")
        return readln()
    }

    fun wrapperYesOrNot(): String {
        print("[${GREEN}Y$RESET/${RED}n$RESET] - ")
        return readln()
    }

    fun wrongAction() {
        logger.warn { "Ты ввел ересь, давай по новой" }
    }

    fun error(exception: Exception) {
        logger.error(exception) { "Дядя, у нас какая-то хрень случилась..." }
    }

}
package ru.artemev.littlecollector.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger { }

@Component
class PrinterHelper {

    companion object {
        private const val GREEN = "\u001b[32m"
        private const val RED = "\u001b[31m"
        private const val RESET = "\u001B[0m"
    }

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
        logger.error { "Дядя, у нас какая-то хрень случилась... Error - ${exception.message}" }
    }
}
package ru.artemev.littlecollector.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import ru.artemev.littlecollector.service.InterfaceService

private val logger = KotlinLogging.logger {}

abstract class AbstractInterfaceServiceImpl : InterfaceService {

    companion object {
        private const val GREEN = "\u001b[32m"
        private const val RED = "\u001b[31m"
        private const val RESET = "\u001B[0m"
    }

    abstract override fun printHello()

    abstract override fun printMenu()

    override fun printOtherTry() {
        logger.info { "Пробанем еще разок?" }
    }

    override fun wrapperInput(): String {
        print("[${GREEN}Input$RESET] -> ")
        return readln()
    }

    override fun wrapperYesOrNot(): String {
        print("[${GREEN}Y$RESET/${RED}n$RESET] - ")
        return readln()
    }

    override fun wrongAction() {
        logger.warn { "Ты ввел ересь, давай по новой" }
    }

    override fun error(exception: Exception) {
        logger.error { "Дядя, у нас какая-то хрень случилась... Error - ${exception.message}" }
    }
}
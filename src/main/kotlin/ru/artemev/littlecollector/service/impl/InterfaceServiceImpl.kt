package ru.artemev.littlecollector.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import ru.artemev.littlecollector.service.InterfaceService

private val logger = KotlinLogging.logger {}

@Service
class InterfaceServiceImpl : InterfaceService {

    companion object {
        const val GREEN = "\u001b[32m"
        const val RESET = "\u001B[0m"
    }

    override fun printHello() {
        logger.info {
            "Привет, введи чего нужно:\n" +
                    "\t1 - скачать немного теневого раба\n" +
                    "\t2 - сходить нахер и закрыть прогу"
        }
    }

    override fun printShadowSlaveHello() {
        logger.info { "Получается качаем теневого раба..." }
    }

    override fun shadowSlaveMenu() {
        logger.info {
            "Че делаем?\n" +
                    "\t1 - Какая последняя глава на сайте?\n" +
                    "\t2 - Давай качать главы"
        }
    }

    override fun wrapperInput(): String {
        print("[${GREEN}Input$RESET] -> ")
        return readln()
    }

    override fun wrongAction() {
        logger.warn { "Ты ввел ересь, давай по новой" }
    }
}
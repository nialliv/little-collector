package ru.artemev.littlecollector.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service("beginInterfaceService")
class BeginInterfaceServiceImpl : AbstractInterfaceServiceImpl() {

    override fun printHello() {
        logger.info { "Привет, чего делать будем?" }
    }

    override fun printMenu() {
        logger.info {
            "Возможностей пока не так много, выбирай:\n" +
                    "\t1 - скачать немного теневого раба\n" +
                    "\t2 - сходить нахер и закрыть прогу"
        }
    }

}
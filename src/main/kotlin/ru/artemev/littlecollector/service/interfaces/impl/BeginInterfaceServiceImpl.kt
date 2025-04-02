package ru.artemev.littlecollector.service.interfaces.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import ru.artemev.littlecollector.enums.ServicesEnum
import ru.artemev.littlecollector.service.interfaces.AbstractInterfaceServiceImpl

private val logger = KotlinLogging.logger {}

@Service("beginInterfaceService")
class BeginInterfaceServiceImpl : AbstractInterfaceServiceImpl() {

    override fun printHello() {
        logger.info { "Привет, чего делать будем?" }
    }

    override fun printMenu() {
        logger.info {
            "Возможностей пока не так много, выбирай:\n" +
                    "\t${ServicesEnum.SHADOW_SLAVE.code} - скачать немного теневого раба\n" +
                    "\t${ServicesEnum.LORD_OF_THE_MYSTERIES.code} - скачать повелителя тайн\n" +
                    "\t${ServicesEnum.EXIT.code} - сходить нахер и закрыть прогу"
        }
    }

}
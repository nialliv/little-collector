package ru.artemev.littlecollector.service.interfaces.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import ru.artemev.littlecollector.enums.InterfaceResponseEnum
import ru.artemev.littlecollector.service.interfaces.AbstractInterfaceServiceImpl

private val logger = KotlinLogging.logger {}

@Service("beginInterfaceService")
class BeginInterfaceServiceImpl : AbstractInterfaceServiceImpl() {

    override fun printHello() {
        logger.info { "Привет, чего делать будем?" }
    }

    //todo refactor printing by enum
    override fun printMenu() {
        logger.info {
            "Возможностей пока не так много, выбирай:\n" +
                    "\t${InterfaceResponseEnum.SHADOW_SLAVE.code} - скачать немного теневого раба\n" +
                    "\t${InterfaceResponseEnum.LORD_OF_THE_MYSTERIES.code} - скачать повелителя тайн\n" +
                    "\t${InterfaceResponseEnum.EXIT.code} - сходить нахер и закрыть прогу"
        }
    }

}
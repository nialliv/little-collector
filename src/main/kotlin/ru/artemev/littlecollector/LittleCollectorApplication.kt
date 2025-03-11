package ru.artemev.littlecollector

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LittleCollectorApplication

fun main(args: Array<String>) {
    runApplication<LittleCollectorApplication>(*args)
}

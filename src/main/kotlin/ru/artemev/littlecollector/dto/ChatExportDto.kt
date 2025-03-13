package ru.artemev.littlecollector.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatExportDto(

    val name: String,

    val messages: List<ChatMessageItem>
)

package ru.artemev.littlecollector.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageItem(

    val type: String,

    @SerialName("text_entities")
    val textEntities: List<TextEntity>
)
package ru.artemev.littlecollector.dto

import kotlinx.serialization.Serializable

@Serializable
data class TextEntity (

    val type: String,

    val text: String,

    val href: String? = null
)
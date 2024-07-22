package com.example.project

data class Flashcard(
    var id: String = "",
    var content: String = "",
    var definition: String = "",
    var imageUrl: String? = null,
    var spanDataContent: List<SpanData>? = null,
    var spanDataDefinition: List<SpanData>? = null,
    var creatorId: String = ""
)


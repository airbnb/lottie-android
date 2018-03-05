package com.airbnb.lottie.samples.model

data class CollectionsResponse(
        val id: Long,
        val title: String,
        val authorId: Long,
        val tag: String,
        val slug: String,
        val description: String,
        val entries: List<AnimationData>
)
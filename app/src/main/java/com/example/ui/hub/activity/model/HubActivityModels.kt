package com.example.ui.hub.activity.model

data class HubActivityEvent(
    val eventId: String = "",
    val hubId: String = "",
    val actorUserId: String = "",
    val actorName: String = "",
    val actorAvatarType: String = "MALE",
    val eventType: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)

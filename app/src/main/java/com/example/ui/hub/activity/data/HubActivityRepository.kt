package com.example.ui.hub.activity.data

import com.example.ui.hub.activity.model.HubActivityEvent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID

object HubActivityRepository {
    private val firestore: FirebaseFirestore?
        get() = try { FirebaseFirestore.getInstance() } catch (_: Exception) { null }

    private val localEventsMap = mutableMapOf<String, MutableList<HubActivityEvent>>()
    private val _events = MutableStateFlow<List<HubActivityEvent>>(emptyList())
    val events: StateFlow<List<HubActivityEvent>> = _events.asStateFlow()

    private var activeHubId: String? = null
    private var listenerRegistration: ListenerRegistration? = null
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    fun attachListener(hubId: String) {
        if (activeHubId == hubId && listenerRegistration != null) return
        detachListener()
        activeHubId = hubId

        _events.value = localEventsMap[hubId]?.sortedByDescending { it.timestamp } ?: emptyList()

        val db = firestore ?: return
        try {
            listenerRegistration = db.collection("family_hubs")
                .document(hubId)
                .collection("hub_activity")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val serverList = snapshot.documents.mapNotNull { doc ->
                            try {
                                HubActivityEvent(
                                    eventId = doc.getString("eventId") ?: doc.id,
                                    hubId = doc.getString("hubId") ?: hubId,
                                    actorUserId = doc.getString("actorUserId") ?: "",
                                    actorName = doc.getString("actorName") ?: "Family Member",
                                    actorAvatarType = doc.getString("actorAvatarType") ?: "MALE",
                                    eventType = doc.getString("eventType") ?: "",
                                    targetType = doc.getString("targetType") ?: "",
                                    targetId = doc.getString("targetId") ?: "",
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    metadata = (doc.get("metadata") as? Map<*, *>)?.entries?.associate {
                                        it.key.toString() to (it.value?.toString() ?: "")
                                    } ?: emptyMap()
                                )
                            } catch (_: Exception) {
                                null
                            }
                        }

                        val localList = localEventsMap[hubId] ?: mutableListOf()
                        val combined = (serverList + localList).distinctBy { it.eventId }.sortedByDescending { it.timestamp }
                        localEventsMap[hubId] = combined.toMutableList()
                        _events.value = combined
                    }
                }
        } catch (_: Exception) {}
    }

    fun detachListener() {
        try {
            listenerRegistration?.remove()
        } catch (_: Exception) {}
        listenerRegistration = null
        activeHubId = null
    }

    fun recordEvent(
        hubId: String,
        actorUserId: String,
        actorName: String,
        actorAvatarType: String,
        eventType: String,
        targetType: String,
        targetId: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        if (hubId.isBlank()) return
        val eventId = "${eventType}_${targetId}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val event = HubActivityEvent(
            eventId = eventId,
            hubId = hubId,
            actorUserId = actorUserId,
            actorName = actorName.ifBlank { "Family Member" },
            actorAvatarType = actorAvatarType,
            eventType = eventType,
            targetType = targetType,
            targetId = targetId,
            timestamp = System.currentTimeMillis(),
            metadata = metadata
        )

        val list = localEventsMap.getOrPut(hubId) { mutableListOf() }
        list.add(0, event)
        if (activeHubId == hubId) {
            _events.value = list.sortedByDescending { it.timestamp }
        }

        repositoryScope.launch {
            try {
                val db = firestore ?: return@launch
                val docData = mapOf(
                    "eventId" to event.eventId,
                    "hubId" to event.hubId,
                    "actorUserId" to event.actorUserId,
                    "actorName" to event.actorName,
                    "actorAvatarType" to event.actorAvatarType,
                    "eventType" to event.eventType,
                    "targetType" to event.targetType,
                    "targetId" to event.targetId,
                    "timestamp" to event.timestamp,
                    "metadata" to event.metadata
                )
                db.collection("family_hubs")
                    .document(hubId)
                    .collection("hub_activity")
                    .document(eventId)
                    .set(docData, SetOptions.merge())
                    .await()
            } catch (_: Exception) {}
        }
    }

    fun getEventsForDate(hubId: String, calendar: Calendar): List<HubActivityEvent> {
        val allEvents = localEventsMap[hubId] ?: _events.value
        val targetYear = calendar.get(Calendar.YEAR)
        val targetMonth = calendar.get(Calendar.MONTH)
        val targetDay = calendar.get(Calendar.DAY_OF_MONTH)

        return allEvents.filter { event ->
            val evCal = Calendar.getInstance().apply { timeInMillis = event.timestamp }
            evCal.get(Calendar.YEAR) == targetYear &&
                    evCal.get(Calendar.MONTH) == targetMonth &&
                    evCal.get(Calendar.DAY_OF_MONTH) == targetDay
        }.sortedByDescending { it.timestamp }
    }
}

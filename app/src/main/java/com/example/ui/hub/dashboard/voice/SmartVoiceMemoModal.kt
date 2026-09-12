package com.example.ui.hub.dashboard.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.storage.FirebaseStorageService
import com.example.ui.hub.dashboard.model.HubMember
import com.example.ui.hub.dashboard.model.ReminderCycle
import com.example.ui.hub.dashboard.components.ClockTimePickerDialog
import com.example.ui.profilesetup.components.ProfileAvatarView
import com.example.ui.theme.SoraFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorPaleTeal = Color(0xFFA9CED0)
private val ColorDarkWarmText = Color(0xFF514A44)
private val ColorTextMuted = Color(0xFF665F58)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorBorderActive = Color(0xFF72B5BA)
private val ColorMutedTerracotta = Color(0xFFC8755D)
private val ColorBurntApricot = Color(0xFFD88B3D)

enum class VoiceMemoState {
    INTRO,
    LISTENING,
    TRANSCRIPT_PROCESSING,
    DRAFT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartVoiceMemoModal(
    approvedMembers: List<HubMember>,
    currentUserId: String,
    currentUserName: String,
    onSaveMedication: (
        name: String,
        dosage: String,
        recipient: HubMember,
        reminderTime: String,
        reminderHour: Int,
        reminderMinute: Int,
        reminderCycle: ReminderCycle,
        customIntervalDays: Int,
        notes: String?,
        imageUri: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentState by remember { mutableStateOf(VoiceMemoState.INTRO) }
    var transcriptText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Draft editable fields matching AddEditMedicationModal
    var draftMedicineName by remember { mutableStateOf("") }
    var draftDosage by remember { mutableStateOf("1 tablet") }
    var selectedRecipient by remember { mutableStateOf<HubMember?>(null) }
    var ambiguousMembersList by remember { mutableStateOf<List<HubMember>>(emptyList()) }
    var draftReminderHour by remember { mutableIntStateOf(9) }
    var draftReminderMinute by remember { mutableIntStateOf(0) }
    var draftReminderCycle by remember { mutableStateOf(ReminderCycle.EVERY_24_HOURS) }
    var customIntervalDays by remember { mutableIntStateOf(3) }
    var draftNotes by remember { mutableStateOf("") }
    var imageUriString by remember { mutableStateOf<String?>(null) }

    var isTimePickerOpen by remember { mutableStateOf(false) }
    var isRecipientMenuExpanded by remember { mutableStateOf(false) }
    var isNotesExpanded by remember { mutableStateOf(false) }
    var isPhotoSourceDialogOpen by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Speech recognizer helper
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
        }
    }

    // Camera & Gallery launchers for draft photo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUriString = uri.toString()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val tempFile = File(context.cacheDir, "med_photo_${System.currentTimeMillis()}.jpg")
                tempFile.outputStream().use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                }
                imageUriString = Uri.fromFile(tempFile).toString()
            } catch (_: Exception) {}
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    fun stopListeningAndProcess() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
    }

    fun startListeningSession() {
        errorMessage = null
        if (speechRecognizer == null) {
            errorMessage = "Speech recognition unavailable on this device."
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 20000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000L)
        }

        try {
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    currentState = VoiceMemoState.LISTENING
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    if (currentState == VoiceMemoState.LISTENING) {
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "Couldn't hear anything. Try again."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Try again."
                            SpeechRecognizer.ERROR_NETWORK -> "Network error during recognition."
                            else -> null
                        }
                        if (msg != null) {
                            errorMessage = msg
                            currentState = VoiceMemoState.INTRO
                        }
                    }
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        transcriptText = text
                        currentState = VoiceMemoState.TRANSCRIPT_PROCESSING
                        coroutineScope.launch {
                            delay(600)
                            val draft = SmartVoiceMemoExtractor.parseTranscript(text, currentUserId, currentUserName, approvedMembers)
                            draftMedicineName = draft.medicineName
                            draftDosage = draft.dosage.ifBlank { "1 tablet" }
                            selectedRecipient = draft.recipient
                            ambiguousMembersList = draft.ambiguousHubMembers
                            draftReminderHour = draft.reminderHour
                            draftReminderMinute = draft.reminderMinute
                            draftReminderCycle = draft.reminderCycle
                            draftNotes = draft.notes ?: ""
                            isNotesExpanded = draft.notes?.isNotBlank() == true
                            currentState = VoiceMemoState.DRAFT
                        }
                    } else {
                        errorMessage = "Couldn't hear anything. Try again."
                        currentState = VoiceMemoState.INTRO
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        transcriptText = matches[0]
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            errorMessage = "Failed to start speech recognition."
            currentState = VoiceMemoState.INTRO
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListeningSession()
        } else {
            errorMessage = "Microphone permission is required for Smart Voice Memo."
        }
    }

    fun onMicClicked() {
        when (currentState) {
            VoiceMemoState.INTRO -> {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    startListeningSession()
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
            VoiceMemoState.LISTENING -> {
                // User explicitly stops recording
                stopListeningAndProcess()
            }
            else -> {}
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ColorWarmIvory,
        scrimColor = Color(0x701F1D1B),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ColorBorderWarm)
            )
        },
        modifier = Modifier
            .imePadding()
            .testTag("smart_voice_memo_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: SMART VOICE MEMO heading (prominent) + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SMART VOICE MEMO",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = ColorDarkWarmText,
                    letterSpacing = 1.2.sp
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ColorWarmCream)
                        .testTag("smart_voice_memo_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = ColorDarkWarmText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorMutedTerracotta.copy(alpha = 0.12f))
                        .border(BorderStroke(1.dp, ColorMutedTerracotta), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        fontFamily = SoraFontFamily,
                        fontSize = 12.sp,
                        color = ColorMutedTerracotta,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            when (currentState) {
                VoiceMemoState.INTRO -> {
                    Text(
                        text = "Speak naturally and we'll fill in the medication details for you.",
                        fontFamily = SoraFontFamily,
                        fontSize = 12.5.sp,
                        color = ColorTextMuted,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ColorWarmCream)
                            .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "PREFERRED WAY TO SPEAK",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = ColorTextMuted,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "“I have to take Naproxen, 1 tablet, at 9:00 AM.”",
                            fontFamily = SoraFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorDarkWarmText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "or",
                            fontFamily = SoraFontFamily,
                            fontSize = 11.sp,
                            color = ColorTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "“Liam has to take Naproxen, 1 tablet, at 9:00 AM.”",
                            fontFamily = SoraFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorDarkWarmText
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(role = Role.Button) { onMicClicked() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .background(ColorWarmAmber)
                                .testTag("smart_voice_memo_mic_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Mic,
                                contentDescription = "Press to speak",
                                tint = ColorWarmIvory,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Press to speak",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ColorDarkWarmText
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    SubtleWatermarkBackground()
                    Spacer(modifier = Modifier.height(12.dp))
                }

                VoiceMemoState.LISTENING -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Listening... Tap to stop",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = ColorDustyTeal
                    )
                    if (transcriptText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "“$transcriptText”",
                            fontFamily = SoraFontFamily,
                            fontSize = 12.5.sp,
                            color = ColorTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(ColorDustyTeal)
                            .clickable(role = Role.Button) { onMicClicked() }
                            .testTag("smart_voice_memo_stop_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MicNone,
                            contentDescription = "Tap to stop recording",
                            tint = ColorWarmIvory,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tap microphone to finish",
                        fontFamily = SoraFontFamily,
                        fontSize = 12.sp,
                        color = ColorTextMuted
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                }

                VoiceMemoState.TRANSCRIPT_PROCESSING -> {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "I HEARD",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        color = ColorTextMuted,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "“$transcriptText”",
                        fontFamily = SoraFontFamily,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorDarkWarmText,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))
                    ThreeDotPulseIndicator()
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Processing...",
                        fontFamily = SoraFontFamily,
                        fontSize = 12.sp,
                        color = ColorTextMuted
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                }

                VoiceMemoState.DRAFT -> {
                    // Reusing exact Add Medication form structure & fields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DRAFT MEDICATION",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ColorDarkWarmText
                            )
                            Text(
                                text = "Review and edit details before creating.",
                                fontFamily = SoraFontFamily,
                                fontSize = 12.sp,
                                color = ColorTextMuted
                            )
                        }

                        TextButton(
                            onClick = {
                                currentState = VoiceMemoState.INTRO
                                transcriptText = ""
                                errorMessage = null
                            },
                            modifier = Modifier.testTag("smart_voice_memo_start_over_button")
                        ) {
                            Text(
                                text = "Start over",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ColorDustyTeal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Medicine Photo Upload Area (Optional)
                    Text(
                        text = "Medicine photo (optional)",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = ColorDarkWarmText
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (!imageUriString.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ColorWarmCream)
                                .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(14.dp))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageUriString)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Selected medicine photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xCC333333))
                                    .clickable { imageUriString = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Remove photo",
                                    tint = ColorWarmIvory,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ColorWarmCream)
                                .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(12.dp))
                                .clickable(role = Role.Button) { isPhotoSourceDialogOpen = true }
                                .padding(horizontal = 16.dp)
                                .testTag("draft_photo_upload_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddAPhoto,
                                    contentDescription = null,
                                    tint = ColorDustyTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "+ Add medicine photo",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = ColorDustyTeal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Medicine Name (Required)
                    Text(
                        text = "Medicine name",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = ColorDarkWarmText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = draftMedicineName,
                        onValueChange = { draftMedicineName = it },
                        textStyle = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = ColorDarkWarmText
                        ),
                        placeholder = {
                            Text(
                                text = "Enter medicine name",
                                fontFamily = SoraFontFamily,
                                fontSize = 12.5.sp,
                                color = ColorTextMuted.copy(alpha = 0.65f)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ColorWarmCream,
                            unfocusedContainerColor = ColorWarmCream,
                            focusedBorderColor = ColorBorderActive,
                            unfocusedBorderColor = ColorBorderWarm
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("draft_medicine_name_input")
                    )
                    if (draftMedicineName.isBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Required. Please enter medicine name.",
                            fontSize = 11.sp,
                            color = ColorMutedTerracotta
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. For Whom Selector
                    Text(
                        text = "For whom?",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = ColorDarkWarmText
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (ambiguousMembersList.isNotEmpty()) {
                        Text(
                            text = "Multiple family members matched. Please select one:",
                            fontFamily = SoraFontFamily,
                            fontSize = 11.5.sp,
                            color = ColorMutedTerracotta
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ambiguousMembersList.forEach { member ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ColorWarmCream)
                                        .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedRecipient = member
                                            ambiguousMembersList = emptyList()
                                        }
                                        .padding(10.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ProfileAvatarView(avatarType = member.avatarType, size = 24.dp)
                                        Text(
                                            text = member.name,
                                            fontFamily = SoraFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = ColorDarkWarmText
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ColorWarmCream)
                                    .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(12.dp))
                                    .clickable(role = Role.Button) { isRecipientMenuExpanded = true }
                                    .padding(horizontal = 12.dp)
                                    .testTag("draft_recipient_selector_button"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (selectedRecipient != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ProfileAvatarView(avatarType = selectedRecipient!!.avatarType, size = 24.dp)
                                        Text(
                                            text = selectedRecipient!!.name,
                                            fontFamily = SoraFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp,
                                            color = ColorDarkWarmText
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Select family member",
                                        fontFamily = SoraFontFamily,
                                        fontSize = 12.5.sp,
                                        color = ColorTextMuted
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = ColorTextMuted
                                )
                            }

                            DropdownMenu(
                                expanded = isRecipientMenuExpanded,
                                onDismissRequest = { isRecipientMenuExpanded = false },
                                shape = RoundedCornerShape(12.dp),
                                containerColor = ColorWarmIvory,
                                border = BorderStroke(1.2.dp, ColorBorderWarm)
                            ) {
                                approvedMembers.forEach { member ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                ProfileAvatarView(avatarType = member.avatarType, size = 26.dp)
                                                Column {
                                                    Text(
                                                        text = member.name,
                                                        fontFamily = SoraFontFamily,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 13.sp,
                                                        color = ColorDarkWarmText
                                                    )
                                                    if (member.isChild) {
                                                        Text(
                                                            text = "Child Dependent",
                                                            fontFamily = SoraFontFamily,
                                                            fontSize = 10.sp,
                                                            color = ColorWarmAmber
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedRecipient = member
                                            isRecipientMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Dosage
                    Text(
                        text = "Dosage",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = ColorDarkWarmText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = draftDosage,
                        onValueChange = { draftDosage = it },
                        textStyle = TextStyle(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = ColorDarkWarmText
                        ),
                        placeholder = {
                            Text(
                                text = "e.g. 1 tablet",
                                fontFamily = SoraFontFamily,
                                fontSize = 12.5.sp,
                                color = ColorTextMuted.copy(alpha = 0.65f)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ColorWarmCream,
                            unfocusedContainerColor = ColorWarmCream,
                            focusedBorderColor = ColorBorderActive,
                            unfocusedBorderColor = ColorBorderWarm
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("draft_dosage_input")
                    )
                    if (draftDosage.isBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Required. Please enter dosage.",
                            fontSize = 11.sp,
                            color = ColorMutedTerracotta
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. Reminder Time
                    val formattedSelectedTime = formatTimeLabel(draftReminderHour, draftReminderMinute)
                    Text(
                        text = "Reminder time",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = ColorDarkWarmText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorWarmCream)
                            .border(BorderStroke(1.2.dp, ColorBorderWarm), RoundedCornerShape(12.dp))
                            .clickable(role = Role.Button) { isTimePickerOpen = true }
                            .padding(horizontal = 12.dp)
                            .testTag("draft_reminder_time_picker_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = ColorDustyTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = formattedSelectedTime,
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = ColorDarkWarmText
                            )
                        }
                        Text(
                            text = "Change",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = ColorDustyTeal
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6. Reminder Cycle
                    Text(
                        text = "Reminder cycle",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = ColorDarkWarmText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorWarmCream)
                            .border(BorderStroke(1.dp, ColorBorderWarm), RoundedCornerShape(12.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ReminderCycle.values().forEach { cycle ->
                            val isSelected = draftReminderCycle == cycle
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (isSelected) ColorDustyTeal else Color.Transparent)
                                    .clickable(role = Role.Button) { draftReminderCycle = cycle }
                                    .testTag("draft_cycle_${cycle.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cycle.label,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 11.5.sp,
                                    color = if (isSelected) ColorWarmIvory else ColorDarkWarmText
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = draftReminderCycle == ReminderCycle.CUSTOM,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = "Repeat every $customIntervalDays days",
                                fontFamily = SoraFontFamily,
                                fontSize = 12.sp,
                                color = ColorTextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(2, 3, 5, 7).forEach { days ->
                                    val isChosen = customIntervalDays == days
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isChosen) ColorDustyTeal.copy(alpha = 0.2f) else ColorWarmCream)
                                            .border(
                                                BorderStroke(1.dp, if (isChosen) ColorDustyTeal else ColorBorderWarm),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { customIntervalDays = days }
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$days days",
                                            fontFamily = SoraFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.5.sp,
                                            color = if (isChosen) ColorDustyTeal else ColorDarkWarmText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7. Notes (Optional Collapsible)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) { isNotesExpanded = !isNotesExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notes,
                                contentDescription = null,
                                tint = ColorTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Notes (optional)",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = ColorDarkWarmText
                            )
                        }
                        Icon(
                            imageVector = if (isNotesExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = ColorTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = isNotesExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            OutlinedTextField(
                                value = draftNotes,
                                onValueChange = { draftNotes = it },
                                textStyle = TextStyle(
                                    fontFamily = SoraFontFamily,
                                    fontSize = 13.sp,
                                    color = ColorDarkWarmText
                                ),
                                placeholder = {
                                    Text(
                                        text = "e.g. Take with food",
                                        fontFamily = SoraFontFamily,
                                        fontSize = 12.sp,
                                        color = ColorTextMuted.copy(alpha = 0.65f)
                                    )
                                },
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = ColorWarmCream,
                                    unfocusedContainerColor = ColorWarmCream,
                                    focusedBorderColor = ColorBorderActive,
                                    unfocusedBorderColor = ColorBorderWarm
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("draft_notes_input")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 8. Create Medication Button matching Add Medication flow
                    val isFormValid = draftMedicineName.trim().isNotBlank() &&
                            draftDosage.trim().isNotBlank() &&
                            selectedRecipient != null &&
                            ambiguousMembersList.isEmpty()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(if (isFormValid && !isSaving) ColorWarmAmber else ColorWarmAmber.copy(alpha = 0.45f))
                            .clickable(
                                enabled = isFormValid && !isSaving,
                                role = Role.Button,
                                onClick = {
                                    if (isFormValid && selectedRecipient != null) {
                                        isSaving = true
                                        coroutineScope.launch {
                                            val medId = UUID.randomUUID().toString()
                                            val hubId = "hub_shared"
                                            val finalImageUri = if (!imageUriString.isNullOrBlank() &&
                                                (imageUriString!!.startsWith("content://") || imageUriString!!.startsWith("file://"))) {
                                                FirebaseStorageService.uploadMedicationImage(
                                                    context = context,
                                                    sourceUri = Uri.parse(imageUriString!!),
                                                    hubId = hubId,
                                                    medicationId = medId
                                                ) ?: imageUriString
                                            } else {
                                                imageUriString
                                            }

                                            onSaveMedication(
                                                draftMedicineName.trim(),
                                                draftDosage.trim(),
                                                selectedRecipient!!,
                                                formattedSelectedTime,
                                                draftReminderHour,
                                                draftReminderMinute,
                                                draftReminderCycle,
                                                customIntervalDays,
                                                draftNotes.ifBlank { null },
                                                finalImageUri
                                            )
                                        }
                                    }
                                }
                            )
                            .testTag("create_medication_from_voice_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSaving) "Creating..." else "Create Medication",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = ColorWarmIvory
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (isTimePickerOpen) {
        ClockTimePickerDialog(
            initialHour = draftReminderHour,
            initialMinute = draftReminderMinute,
            onConfirmTime = { h, m ->
                draftReminderHour = h
                draftReminderMinute = m
                isTimePickerOpen = false
            },
            onDismiss = { isTimePickerOpen = false }
        )
    }

    if (isPhotoSourceDialogOpen) {
        AlertDialog(
            onDismissRequest = { isPhotoSourceDialogOpen = false },
            title = {
                Text("Medicine photo", fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Choose photo source", fontFamily = SoraFontFamily)
            },
            confirmButton = {
                TextButton(onClick = {
                    isPhotoSourceDialogOpen = false
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text("Gallery", fontFamily = SoraFontFamily, color = ColorDustyTeal)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    isPhotoSourceDialogOpen = false
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraLauncher.launch(null)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Text("Camera", fontFamily = SoraFontFamily, color = ColorDustyTeal)
                }
            },
            containerColor = ColorWarmIvory
        )
    }
}

private fun formatTimeLabel(hour: Int, minute: Int): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
    return fmt.format(cal.time)
}

@Composable
private fun ThreeDotPulseIndicator() {
    var activeDot by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(240)
            activeDot = (activeDot + 1) % 3
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val isActive = index == activeDot
            Box(
                modifier = Modifier
                    .size(if (isActive) 7.dp else 4.dp)
                    .clip(CircleShape)
                    .background(ColorDustyTeal.copy(alpha = if (isActive) 1.0f else 0.4f))
            )
        }
    }
}

@Composable
private fun SubtleWatermarkBackground() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 24.dp)
    ) {
        val paintColor = ColorPaleTeal.copy(alpha = 0.25f)
        val pillColor = ColorBurntApricot.copy(alpha = 0.15f)
        val spacing = 48.dp.toPx()
        var yPos = 16.dp.toPx()
        var row = 0
        while (yPos < size.height) {
            var xPos = 24.dp.toPx() + if (row % 2 == 0) 0f else 24.dp.toPx()
            while (xPos < size.width - 12.dp.toPx()) {
                if ((row + xPos.toInt()) % 3 == 0) {
                    drawCircle(color = pillColor, radius = 3.dp.toPx(), center = androidx.compose.ui.geometry.Offset(xPos, yPos))
                } else {
                    drawCircle(color = paintColor, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(xPos, yPos))
                }
                xPos += spacing
            }
            yPos += spacing
            row++
        }
    }
}

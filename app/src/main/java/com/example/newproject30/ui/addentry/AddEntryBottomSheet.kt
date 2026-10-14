package com.example.newproject30.ui.addentry

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

private data class RatingOption(
    val score: Int,
    val emoji: String,
    val label: String
)

private val RATING_ITEMS = listOf(
    RatingOption(score = 1, emoji = "😫", label = "Terrible"),
    RatingOption(score = 2, emoji = "🙁", label = "Low"),
    RatingOption(score = 3, emoji = "😐", label = "Neutral"),
    RatingOption(score = 4, emoji = "🙂", label = "Good"),
    RatingOption(score = 5, emoji = "😄", label = "Great")
)

/**
 * Material 3 Modal Bottom Sheet for adding a new Mood & Note entry with continuous live voice transcription and custom emotion tags.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEntryBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEntryViewModel = viewModel(
        factory = AddEntryViewModel.provideFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    // Automatically close bottom sheet when entry is saved
    LaunchedEffect(uiState.isEntrySaved) {
        if (uiState.isEntrySaved) {
            onDismiss()
            viewModel.onEvent(AddEntryUiEvent.ResetSavedState)
        }
    }

    // Continuous SpeechRecognizer Manager with live interim text streaming & auto-restart across pauses
    val voiceToTextManager = rememberContinuousSpeechManager(
        onPartialResult = { partialText ->
            viewModel.onEvent(AddEntryUiEvent.UpdatePartialSpeech(partialText))
        },
        onFinalResult = { finalText ->
            viewModel.onEvent(AddEntryUiEvent.CommitFinalSpeech(finalText))
        },
        onRecordingStateChanged = { isRecording ->
            viewModel.onEvent(AddEntryUiEvent.SetRecordingState(isRecording))
        }
    )

    // Microphone Permission Request Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onEvent(AddEntryUiEvent.SetPermissionGranted(isGranted))
        if (isGranted) {
            voiceToTextManager.startListening()
        } else {
            Toast.makeText(context, "Microphone permission is required for voice notes", Toast.LENGTH_SHORT).show()
        }
    }

    val toggleVoiceDictation = {
        if (uiState.isRecording) {
            voiceToTextManager.stopListening()
        } else {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                viewModel.onEvent(AddEntryUiEvent.SetPermissionGranted(true))
                voiceToTextManager.startListening()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Infinite animation pulse for microphone active recording state
    val infiniteTransition = rememberInfiniteTransition(label = "MicPulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (uiState.isRecording) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MicScale"
    )

    // AlertDialog to Create a New Custom Emotion or Context Tag
    if (uiState.showAddTagDialog) {
        val isEmotion = uiState.tagDialogCategory == "EMOTION"
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(AddEntryUiEvent.DismissAddTagDialog) },
            title = {
                Text(
                    text = if (isEmotion) "Create New Primary Emotion" else "Create New Context Tag",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = uiState.newTagInput,
                    onValueChange = { viewModel.onEvent(AddEntryUiEvent.NewTagInputChanged(it)) },
                    placeholder = { Text(if (isEmotion) "e.g. Inspired, Hopeful..." else "e.g. Creative, Exercise...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(AddEntryUiEvent.SaveNewCustomTag) },
                    enabled = uiState.newTagInput.isNotBlank()
                ) {
                    Text("Save & Select")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onEvent(AddEntryUiEvent.DismissAddTagDialog) }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            voiceToTextManager.cancelListening()
            onDismiss()
        },
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Sheet Title
            Text(
                text = "Log Mood & Note",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Mood Rating Selector (5 Discrete Options with equal width weight(1f))
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RATING_ITEMS.forEach { item ->
                    val isSelected = uiState.moodScore == item.score

                    Surface(
                        selected = isSelected,
                        onClick = { viewModel.onEvent(AddEntryUiEvent.SetMoodScore(item.score)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(68.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        contentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.emoji,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Dynamic Primary Emotion FilterChips + "+ Add Emotion" AssistChip
            Text(
                text = "Primary Emotion",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                uiState.availableEmotions.forEach { emotion ->
                    val isSelected = uiState.primaryEmotion == emotion
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onEvent(AddEntryUiEvent.SetPrimaryEmotion(emotion)) },
                        label = {
                            Text(
                                text = emotion,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }

                // "+ Add Emotion" AssistChip
                AssistChip(
                    onClick = { viewModel.onEvent(AddEntryUiEvent.ShowAddTagDialog("EMOTION")) },
                    label = {
                        Text(
                            text = "+ Add Emotion",
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Custom Emotion",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Category / Contextual Tags Section
            Text(
                text = "Tags / Context",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                uiState.availableContextTags.forEach { tag ->
                    val isSelected = uiState.selectedTags.contains(tag)
                    InputChip(
                        selected = isSelected,
                        onClick = { viewModel.onEvent(AddEntryUiEvent.ToggleTag(tag)) },
                        label = {
                            Text(
                                text = "#$tag",
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                IconButton(
                                    onClick = { viewModel.onEvent(AddEntryUiEvent.RemoveTag(tag)) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Unselect $tag"
                                    )
                                }
                            }
                        } else null
                    )
                }

                // "+ Add Tag" AssistChip
                AssistChip(
                    onClick = { viewModel.onEvent(AddEntryUiEvent.ShowAddTagDialog("CONTEXT")) },
                    label = {
                        Text(
                            text = "+ Add Tag",
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Custom Tag",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Note Content Text Input & Manual Controls Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Journal Note",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Explicit Cancel Button when recording
                    if (uiState.isRecording) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            IconButton(
                                onClick = {
                                    voiceToTextManager.cancelListening()
                                    viewModel.onEvent(AddEntryUiEvent.CancelSpeech)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Dictation",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Persistent Mic Button: Idle (Tap to Start) / Active (Pulsing Red: Tap to Stop & Commit)
                    Surface(
                        shape = CircleShape,
                        color = if (uiState.isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.scale(micScale)
                    ) {
                        IconButton(onClick = toggleVoiceDictation) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = if (uiState.isRecording) "Stop & Save Dictation" else "Start Voice Dictation",
                                tint = if (uiState.isRecording) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.displayContent,
                onValueChange = { viewModel.onEvent(AddEntryUiEvent.ContentChanged(it)) },
                placeholder = { Text("Type your note or tap the microphone icon for continuous dictation...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            if (uiState.isVoiceTranscript || uiState.isRecording) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (uiState.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (uiState.isRecording) "Listening live... Speak freely" else "Voice transcript attached",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Save Button
            Button(
                onClick = { viewModel.onEvent(AddEntryUiEvent.SaveEntry) },
                enabled = uiState.isSaveEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text(
                    text = "Save Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

package com.example.newproject30.ui.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.newproject30.data.local.entity.MoodEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main Notes screen composable with search, primary emotion filter chips, and bookmark toggling.
 */
@Composable
fun NotesScreen(
    onEntryClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = viewModel(
        factory = NotesViewModel.provideFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NotesContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onEntryClick = onEntryClick,
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    )
}

@Composable
fun NotesContent(
    uiState: NotesUiState,
    onEvent: (NotesUiEvent) -> Unit,
    onEntryClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Sticky Header / Search Input & Bookmark Toggle Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { onEvent(NotesUiEvent.SearchQueryChanged(it)) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search notes or emotions...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onEvent(NotesUiEvent.SearchQueryChanged("")) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Bookmark Filter Toggle Button
            IconToggleButton(
                checked = uiState.showBookmarkedOnly,
                onCheckedChange = { onEvent(NotesUiEvent.ToggleBookmarkFilter) }
            ) {
                Icon(
                    imageVector = if (uiState.showBookmarkedOnly) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Filter Bookmarked Notes",
                    tint = if (uiState.showBookmarkedOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Horizontal Primary Emotion Filter Chips
        AnimatedVisibility(visible = uiState.availableEmotions.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                item(key = "all_emotion") {
                    FilterChip(
                        selected = uiState.selectedEmotion == null,
                        onClick = { onEvent(NotesUiEvent.SelectEmotion(null)) },
                        label = {
                            Text(
                                text = "All",
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }

                items(
                    items = uiState.availableEmotions,
                    key = { it }
                ) { emotion ->
                    FilterChip(
                        selected = uiState.selectedEmotion == emotion,
                        onClick = { onEvent(NotesUiEvent.SelectEmotion(emotion)) },
                        label = {
                            Text(
                                text = emotion,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }

        // Content / Empty States / List
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                uiState.entries.isEmpty() -> {
                    EmptyNotesState(
                        searchQuery = uiState.searchQuery,
                        hasEmotionFilter = uiState.selectedEmotion != null,
                        hasBookmarkFilter = uiState.showBookmarkedOnly
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.entries,
                            key = { it.id }
                        ) { entry ->
                            NoteCard(
                                entry = entry,
                                onCardClick = { onEntryClick(entry.id) },
                                onToggleBookmark = { onEvent(NotesUiEvent.ToggleBookmark(entry.id)) },
                                onDeleteEntry = { onEvent(NotesUiEvent.DeleteEntry(entry.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Note item card displaying entry details and actions. Compressed with maxLines = 2.
 */
@Composable
fun NoteCard(
    entry: MoodEntry,
    onCardClick: () -> Unit = {},
    onToggleBookmark: () -> Unit = {},
    onDeleteEntry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(entry.timestamp) {
        SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
            .format(Date(entry.timestamp))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Primary Emotion Badge, Score & Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = onCardClick,
                        label = {
                            Text(
                                text = entry.primaryEmotion,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Mood: ${entry.moodScore}/5",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Voice Transcript Badge if applicable
            if (entry.isVoiceTranscript) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Voice Transcript",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Snippet compressed to maxLines = 2
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Actions: Tags & Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    entry.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Action Buttons
                Row {
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (entry.isBookmarked) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = if (entry.isBookmarked) "Unbookmark Note" else "Bookmark Note",
                            tint = if (entry.isBookmarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDeleteEntry) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Note",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Empty state component rendered when no notes match search or filters.
 */
@Composable
fun EmptyNotesState(
    searchQuery: String,
    hasEmotionFilter: Boolean,
    hasBookmarkFilter: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.NoteAlt,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(16.dp))

        val titleText = when {
            searchQuery.isNotBlank() -> "No matching notes found"
            hasBookmarkFilter -> "No bookmarked notes"
            hasEmotionFilter -> "No notes with this emotion"
            else -> "Your notes feed is empty"
        }

        val subtitleText = when {
            searchQuery.isNotBlank() -> "Try searching for another keyword or clearing your query."
            hasBookmarkFilter -> "Star notes to save them in your bookmarked list for quick access."
            hasEmotionFilter -> "Select 'All' to view entries from all primary emotions."
            else -> "Tap the voice record button below to log your first mood note!"
        }

        Text(
            text = titleText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitleText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

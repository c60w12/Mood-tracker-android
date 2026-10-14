package com.example.newproject30.ui.dashboard

import androidx.compose.runtime.Immutable
import com.example.newproject30.data.local.entity.MoodEntry

/**
 * Daily mood trend item for visualizer.
 */
@Immutable
data class DailyMoodTrend(
    val dayLabel: String,
    val averageScore: Double
)

/**
 * Breakdown item for emotion distribution.
 */
@Immutable
data class EmotionBreakdownItem(
    val emotion: String,
    val count: Int,
    val percentage: Float
)

/**
 * UI State for the Dashboard & Analytics Screen.
 */
@Immutable
data class DashboardUiState(
    val isLoading: Boolean = true,
    val weeklyAverageMood: Double = 0.0,
    val currentStreakDays: Int = 0,
    val totalCheckInCount: Int = 0,
    val stabilityIndex: String = "Building Data 📊",
    val dominantEmotion: String = "N/A",
    val monthlyDominantEmotion: String = "N/A",
    val weeklyBreakdown: List<EmotionBreakdownItem> = emptyList(),
    val monthlyBreakdown: List<EmotionBreakdownItem> = emptyList(),
    val weeklyTotalEntries: Int = 0,
    val monthlyTotalEntries: Int = 0,
    val isWeeklyBreakdownExpanded: Boolean = false,
    val isMonthlyBreakdownExpanded: Boolean = false,
    val weeklyTrend: List<DailyMoodTrend> = emptyList(),
    val recentEntries: List<MoodEntry> = emptyList()
)

/**
 * User Intents for the Dashboard Screen.
 */
sealed interface DashboardUiEvent {
    data class DeleteEntry(val entryId: Long) : DashboardUiEvent
    data class ToggleBookmark(val entryId: Long) : DashboardUiEvent
    data object ToggleWeeklyBreakdown : DashboardUiEvent
    data object ToggleMonthlyBreakdown : DashboardUiEvent
    data object Refresh : DashboardUiEvent
}

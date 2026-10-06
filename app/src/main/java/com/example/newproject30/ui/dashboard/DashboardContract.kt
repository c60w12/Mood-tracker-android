package com.example.newproject30.ui.dashboard

import androidx.compose.runtime.Immutable
import com.example.newproject30.data.local.entity.MoodEntry

/**
 * Data representation for a single day's mood in the 7-day trend visualizer.
 */
@Immutable
data class DailyMoodTrend(
    val dayLabel: String,
    val averageScore: Double
)

/**
 * Item representing emotion distribution count and percentage.
 */
@Immutable
data class EmotionBreakdownItem(
    val emotion: String,
    val count: Int,
    val percentage: Float
)

/**
 * UI State for the Dashboard & Analytics screen.
 */
@Immutable
data class DashboardUiState(
    val isLoading: Boolean = true,
    val weeklyAverageMood: Double = 0.0,
    val currentStreakDays: Int = 0,
    val dominantEmotion: String = "N/A",
    val monthlyDominantEmotion: String = "N/A",
    val weeklyBreakdown: List<EmotionBreakdownItem> = emptyList(),
    val monthlyBreakdown: List<EmotionBreakdownItem> = emptyList(),
    val weeklyTotalEntries: Int = 0,
    val monthlyTotalEntries: Int = 0,
    val isWeeklyBreakdownExpanded: Boolean = false,
    val isMonthlyBreakdownExpanded: Boolean = false,
    val weeklyTrend: List<DailyMoodTrend> = emptyList(),
    val moodDistribution: Map<String, Int> = emptyMap(),
    val recentEntries: List<MoodEntry> = emptyList()
)

/**
 * User Intents for the Dashboard screen.
 */
sealed interface DashboardUiEvent {
    data object Refresh : DashboardUiEvent
    data class DeleteEntry(val entryId: Long) : DashboardUiEvent
    data class ToggleBookmark(val entryId: Long) : DashboardUiEvent
    data object ToggleWeeklyBreakdown : DashboardUiEvent
    data object ToggleMonthlyBreakdown : DashboardUiEvent
}

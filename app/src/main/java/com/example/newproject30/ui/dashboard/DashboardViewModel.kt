package com.example.newproject30.ui.dashboard

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.newproject30.data.local.AppDatabase
import com.example.newproject30.data.local.dao.MoodStats
import com.example.newproject30.data.local.entity.MoodEntry
import com.example.newproject30.data.repository.MoodRepository
import com.example.newproject30.data.repository.MoodRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class AnalyticsTuple(
    val breakdown14: List<EmotionBreakdownItem>,
    val breakdown30: List<EmotionBreakdownItem>,
    val stats7: MoodStats,
    val totalCheckIns: Int,
    val stabilityText: String,
    val total14: Int,
    val total30: Int
)

/**
 * ViewModel responsible for computing streak, 100% offline Room SQL primary emotion analytics, 14-day stability index, and emotion distribution.
 * All computations run strictly on [Dispatchers.IO] to keep the UI thread jank-free.
 */
class DashboardViewModel(
    private val repository: MoodRepository
) : ViewModel() {

    private val _isWeeklyExpanded = MutableStateFlow(false)
    private val _isMonthlyExpanded = MutableStateFlow(false)

    private val now = System.currentTimeMillis()
    private val sevenDaysAgo = now - (7 * 24 * 3600 * 1000L)
    private val fourteenDaysAgo = now - (14 * 24 * 3600 * 1000L)
    private val thirtyDaysAgo = now - (30 * 24 * 3600 * 1000L)

    private val analyticsFlow = combine(
        repository.getEmotionCountsSince(fourteenDaysAgo),
        repository.getEmotionCountsSince(thirtyDaysAgo),
        repository.getMoodStatsSince(sevenDaysAgo),
        repository.getMoodScoresSince(fourteenDaysAgo),
        repository.getTotalCheckInCount()
    ) { emotion14, emotion30, stats7, scores14, totalCheckIns ->
        val total14 = emotion14.sumOf { it.count }
        val breakdown14 = emotion14.map {
            val pct = if (total14 > 0) it.count.toFloat() / total14 else 0f
            EmotionBreakdownItem(it.emotion, it.count, pct)
        }

        val total30 = emotion30.sumOf { it.count }
        val breakdown30 = emotion30.map {
            val pct = if (total30 > 0) it.count.toFloat() / total30 else 0f
            EmotionBreakdownItem(it.emotion, it.count, pct)
        }

        Log.d("ANALYTICS_DEBUG", "Dashboard UI State Primary Emotions: ${breakdown14.map { "${it.emotion}:${it.count}" }}")

        val stdDev = if (scores14.size >= 2) {
            val mean = scores14.average()
            val variance = scores14.sumOf { (it - mean) * (it - mean) } / scores14.size
            kotlin.math.sqrt(variance)
        } else null

        val stabilityText = when {
            stdDev == null -> "Building Data 📊"
            stdDev < 0.7 -> "High Stability 🌿"
            stdDev <= 1.3 -> "Moderate ⚖️"
            else -> "Fluctuating 🌊"
        }

        AnalyticsTuple(
            breakdown14 = breakdown14,
            breakdown30 = breakdown30,
            stats7 = stats7,
            totalCheckIns = totalCheckIns,
            stabilityText = stabilityText,
            total14 = total14,
            total30 = total30
        )
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        analyticsFlow,
        repository.getAllTimestamps(),
        repository.getAllEntries(),
        _isWeeklyExpanded,
        _isMonthlyExpanded
    ) { analytics, timestamps, allEntries, weeklyExpanded, monthlyExpanded ->
        val streak = calculateStreakFromTimestamps(timestamps)
        val recent = allEntries.take(3)
        val trend = calculateWeeklyTrend(allEntries)

        DashboardUiState(
            isLoading = false,
            weeklyAverageMood = analytics.stats7.avgScore ?: 0.0,
            currentStreakDays = streak,
            totalCheckInCount = analytics.totalCheckIns,
            stabilityIndex = analytics.stabilityText,
            dominantEmotion = analytics.breakdown14.firstOrNull()?.emotion ?: "N/A",
            monthlyDominantEmotion = analytics.breakdown30.firstOrNull()?.emotion ?: "N/A",
            weeklyBreakdown = analytics.breakdown14,
            monthlyBreakdown = analytics.breakdown30,
            weeklyTotalEntries = analytics.total14,
            monthlyTotalEntries = analytics.total30,
            isWeeklyBreakdownExpanded = weeklyExpanded,
            isMonthlyBreakdownExpanded = monthlyExpanded,
            weeklyTrend = trend,
            recentEntries = recent
        )
    }
    .flowOn(Dispatchers.IO)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.DeleteEntry -> {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.deleteEntryById(event.entryId)
                }
            }

            is DashboardUiEvent.ToggleBookmark -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val entry = repository.getEntryById(event.entryId)
                    if (entry != null) {
                        repository.updateEntry(entry.copy(isBookmarked = !entry.isBookmarked))
                    }
                }
            }

            is DashboardUiEvent.ToggleWeeklyBreakdown -> {
                _isWeeklyExpanded.value = !_isWeeklyExpanded.value
            }

            is DashboardUiEvent.ToggleMonthlyBreakdown -> {
                _isMonthlyExpanded.value = !_isMonthlyExpanded.value
            }

            is DashboardUiEvent.Refresh -> {
                // Flow updates automatically
            }
        }
    }

    private fun calculateStreakFromTimestamps(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0
        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val entryDates = timestamps
            .map { dateSdf.format(Date(it)) }
            .distinct()
            .toSet()

        val calendar = Calendar.getInstance()
        val todayStr = dateSdf.format(calendar.time)

        var checkDate = calendar.time
        var streak = 0

        if (!entryDates.contains(todayStr)) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            checkDate = calendar.time
            if (!entryDates.contains(dateSdf.format(checkDate))) {
                return 0
            }
        }

        while (entryDates.contains(dateSdf.format(checkDate))) {
            streak++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            checkDate = calendar.time
        }

        return streak
    }

    private fun calculateWeeklyTrend(entries: List<MoodEntry>): List<DailyMoodTrend> {
        val daySdf = SimpleDateFormat("EEE", Locale.getDefault())
        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val days = (6 downTo 0).map { daysAgo ->
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
            }
            val dateStr = dateSdf.format(cal.time)
            val dayName = daySdf.format(cal.time)
            dateStr to dayName
        }

        val entriesByDate = entries.groupBy { dateSdf.format(Date(it.timestamp)) }

        return days.map { (dateStr, dayName) ->
            val dayEntries = entriesByDate[dateStr] ?: emptyList()
            val avg = if (dayEntries.isNotEmpty()) dayEntries.map { it.moodScore }.average() else 0.0
            DailyMoodTrend(
                dayLabel = dayName,
                averageScore = avg
            )
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(context.applicationContext)
                val repository = MoodRepositoryImpl(database.moodDao(), database.customTagDao(), database.analyticsDao())
                return DashboardViewModel(repository) as T
            }
        }
    }
}

package com.example.newproject30.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.newproject30.data.local.AppDatabase
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

/**
 * ViewModel responsible for computing streak, weekly/monthly averages, and trend analytics for the Dashboard.
 * All computations run strictly on [Dispatchers.IO] to keep the UI thread completely jank-free.
 */
class DashboardViewModel(
    private val repository: MoodRepository
) : ViewModel() {

    private val _isWeeklyExpanded = MutableStateFlow(false)
    private val _isMonthlyExpanded = MutableStateFlow(false)

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getAllEntries(),
        _isWeeklyExpanded,
        _isMonthlyExpanded
    ) { allEntries, weeklyExpanded, monthlyExpanded ->
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - (7 * 24 * 3600 * 1000L)
        val thirtyDaysAgo = now - (30 * 24 * 3600 * 1000L)

        val pastWeekEntries = allEntries.filter { it.timestamp >= sevenDaysAgo }
        val pastMonthEntries = allEntries.filter { it.timestamp >= thirtyDaysAgo }

        val weeklyAvg = if (pastWeekEntries.isNotEmpty()) {
            pastWeekEntries.map { it.moodScore }.average()
        } else {
            0.0
        }

        // Weekly Breakdown & Majority Dominant Emotion
        val weeklyGrouped = pastWeekEntries.groupBy { it.primaryEmotion }
        val weeklyTotal = pastWeekEntries.size
        val weeklyBreakdown = weeklyGrouped.map { (emotion, list) ->
            val count = list.size
            val pct = if (weeklyTotal > 0) count.toFloat() / weeklyTotal else 0f
            EmotionBreakdownItem(emotion, count, pct)
        }.sortedByDescending { it.count }
        val weeklyDominant = weeklyBreakdown.firstOrNull()?.emotion ?: "N/A"

        // Monthly Breakdown & Majority Dominant Emotion
        val monthlyGrouped = pastMonthEntries.groupBy { it.primaryEmotion }
        val monthlyTotal = pastMonthEntries.size
        val monthlyBreakdown = monthlyGrouped.map { (emotion, list) ->
            val count = list.size
            val pct = if (monthlyTotal > 0) count.toFloat() / monthlyTotal else 0f
            EmotionBreakdownItem(emotion, count, pct)
        }.sortedByDescending { it.count }
        val monthlyDominant = monthlyBreakdown.firstOrNull()?.emotion ?: "N/A"

        val streak = calculateStreak(allEntries)
        val trend = calculateWeeklyTrend(allEntries)
        val recent = allEntries.take(3)

        DashboardUiState(
            isLoading = false,
            weeklyAverageMood = weeklyAvg,
            currentStreakDays = streak,
            dominantEmotion = weeklyDominant,
            monthlyDominantEmotion = monthlyDominant,
            weeklyBreakdown = weeklyBreakdown,
            monthlyBreakdown = monthlyBreakdown,
            weeklyTotalEntries = weeklyTotal,
            monthlyTotalEntries = monthlyTotal,
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
                // Flow automatically updates
            }
        }
    }

    private fun calculateStreak(entries: List<MoodEntry>): Int {
        if (entries.isEmpty()) return 0
        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val entryDates = entries
            .map { dateSdf.format(Date(it.timestamp)) }
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
                val repository = MoodRepositoryImpl(database.moodDao())
                return DashboardViewModel(repository) as T
            }
        }
    }
}

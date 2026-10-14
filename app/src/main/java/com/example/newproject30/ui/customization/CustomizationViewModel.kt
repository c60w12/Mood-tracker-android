package com.example.newproject30.ui.customization

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.newproject30.data.local.AppDatabase
import com.example.newproject30.data.local.entity.CustomTag
import com.example.newproject30.data.preferences.UserPreferencesRepository
import com.example.newproject30.data.repository.MoodRepository
import com.example.newproject30.data.repository.MoodRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for theme customization and central Room-synced primary emotions & custom tags management.
 */
class CustomizationViewModel(
    private val repository: MoodRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _dialogState = MutableStateFlow(DialogState())

    private data class DialogState(
        val showDialog: Boolean = false,
        val category: String = "EMOTION",
        val input: String = ""
    )

    val uiState: StateFlow<CustomizationUiState> = combine(
        preferencesRepository.isDarkMode,
        repository.getCustomTagsByCategory("EMOTION"),
        repository.getCustomTagsByCategory("CONTEXT"),
        _dialogState
    ) { isDark, emotions, contextTags, dialogState ->
        CustomizationUiState(
            isDarkMode = isDark,
            primaryEmotions = emotions,
            contextTags = contextTags,
            showAddDialog = dialogState.showDialog,
            dialogCategory = dialogState.category,
            newTagInput = dialogState.input
        )
    }
    .flowOn(Dispatchers.IO)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CustomizationUiState()
    )

    fun onEvent(event: CustomizationUiEvent) {
        when (event) {
            is CustomizationUiEvent.SetDarkMode -> {
                viewModelScope.launch {
                    preferencesRepository.setDarkMode(event.isDark)
                }
            }

            is CustomizationUiEvent.ShowAddDialog -> {
                _dialogState.update { DialogState(showDialog = true, category = event.category, input = "") }
            }

            is CustomizationUiEvent.DismissAddDialog -> {
                _dialogState.update { it.copy(showDialog = false, input = "") }
            }

            is CustomizationUiEvent.NewTagInputChanged -> {
                _dialogState.update { it.copy(input = event.text) }
            }

            is CustomizationUiEvent.SaveNewTag -> {
                val input = _dialogState.value.input.trim()
                val category = _dialogState.value.category
                if (input.isNotBlank()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        val customTag = CustomTag(name = input, isDefault = false, category = category)
                        repository.insertCustomTag(customTag)
                        _dialogState.update { DialogState(showDialog = false, input = "") }
                    }
                }
            }

            is CustomizationUiEvent.DeleteTag -> {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.deleteCustomTag(event.tag)
                }
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(context.applicationContext)
                val moodRepo = MoodRepositoryImpl(database.moodDao(), database.customTagDao(), database.analyticsDao())
                val prefRepo = UserPreferencesRepository(context.applicationContext)
                return CustomizationViewModel(moodRepo, prefRepo) as T
            }
        }
    }
}

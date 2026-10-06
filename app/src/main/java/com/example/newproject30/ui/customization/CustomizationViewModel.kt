package com.example.newproject30.ui.customization

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.newproject30.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for theme customization and custom emotion tags management.
 */
class CustomizationViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _newTagInput = MutableStateFlow("")

    val uiState: StateFlow<CustomizationUiState> = combine(
        preferencesRepository.isDarkMode,
        preferencesRepository.customTags,
        _newTagInput
    ) { isDark, tags, newTagInput ->
        CustomizationUiState(
            isDarkMode = isDark,
            customTags = tags.toList().sorted(),
            newTagInput = newTagInput
        )
    }.stateIn(
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

            is CustomizationUiEvent.NewTagInputChanged -> {
                _newTagInput.value = event.text
            }

            is CustomizationUiEvent.AddTag -> {
                val tag = _newTagInput.value.trim()
                if (tag.isNotBlank()) {
                    viewModelScope.launch {
                        preferencesRepository.addCustomTag(tag)
                        _newTagInput.value = ""
                    }
                }
            }

            is CustomizationUiEvent.RemoveTag -> {
                viewModelScope.launch {
                    preferencesRepository.removeCustomTag(event.tag)
                }
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = UserPreferencesRepository(context.applicationContext)
                return CustomizationViewModel(repository) as T
            }
        }
    }
}

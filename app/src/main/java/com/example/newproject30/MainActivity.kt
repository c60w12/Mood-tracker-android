package com.example.newproject30

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.newproject30.data.preferences.UserPreferencesRepository
import com.example.newproject30.ui.MainScaffold
import com.example.newproject30.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        val hasExtraStartVoice = intent?.getBooleanExtra("EXTRA_START_VOICE", false) ?: false
        val preferencesRepository = UserPreferencesRepository(applicationContext)

        setContent {
            val isDarkMode by preferencesRepository.isDarkMode.collectAsStateWithLifecycle(initialValue = false)
            val autoStartVoiceOnOpen by preferencesRepository.autoStartVoiceOnOpen.collectAsStateWithLifecycle(initialValue = false)

            val autoStartVoice = hasExtraStartVoice || autoStartVoiceOnOpen

            AppTheme(isDarkMode = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScaffold(autoStartVoice = autoStartVoice)
                }
            }
        }
    }
}

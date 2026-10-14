package com.example.newproject30.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.newproject30.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver triggered on system boot to restore 8:00 PM daily reminder exact alarms.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            val repository = UserPreferencesRepository(context.applicationContext)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val remindersEnabled = repository.dailyRemindersEnabled.first()
                    if (remindersEnabled) {
                        ReminderScheduler.scheduleNextDailyReminder(context.applicationContext)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

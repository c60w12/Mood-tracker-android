package com.example.newproject30.data.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Utility helper for scheduling or canceling 24-hour periodic daily reminders via WorkManager.
 */
object ReminderScheduler {

    private const val WORK_TAG = "daily_journal_reminder_work"

    /**
     * Schedules periodic daily reminder worker at target hour/minute (default: 8:00 PM).
     */
    fun scheduleDailyReminder(context: Context, hourOfDay: Int = 20, minute: Int = 0) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = Calendar.getInstance()
        if (calendar.before(now)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val initialDelay = calendar.timeInMillis - now.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Cancels scheduled daily reminder periodic work.
     */
    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(WORK_TAG)
    }

    /**
     * Instantly fires a test notification to verify notification channel, icon, and permission.
     */
    fun sendTestNotification(context: Context) {
        DailyReminderWorker.sendNotification(
            context = context,
            title = "Test Reminder Notification 🔔",
            message = "Notifications are working! You'll receive daily journal reminders."
        )
    }
}

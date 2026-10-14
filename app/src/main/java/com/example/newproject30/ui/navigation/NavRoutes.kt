package com.example.newproject30.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation route destinations for the main application navigation shell.
 */
@Serializable
data object DashboardRoute

@Serializable
data object NotesRoute

@Serializable
data object SettingsRoute

@Serializable
data object CustomizationRoute

@Serializable
data class EntryDetailRoute(val entryId: Long)

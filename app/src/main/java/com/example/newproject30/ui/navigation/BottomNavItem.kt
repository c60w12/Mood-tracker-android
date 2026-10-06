package com.example.newproject30.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Representation of items in the bottom navigation bar.
 *
 * @param label The user-visible title for the navigation item.
 * @param route The type-safe route target object.
 * @param selectedIcon Icon vector displayed when the item is active.
 * @param unselectedIcon Icon vector displayed when the item is inactive.
 */
sealed class BottomNavItem(
    val label: String,
    val route: Any,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Dashboard : BottomNavItem(
        label = "Dashboard",
        route = DashboardRoute,
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )

    data object Notes : BottomNavItem(
        label = "Notes",
        route = NotesRoute,
        selectedIcon = Icons.Filled.Description,
        unselectedIcon = Icons.Outlined.Description
    )

    data object Customization : BottomNavItem(
        label = "Customization",
        route = CustomizationRoute,
        selectedIcon = Icons.Filled.Palette,
        unselectedIcon = Icons.Outlined.Palette
    )

    data object Settings : BottomNavItem(
        label = "Settings",
        route = SettingsRoute,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    companion object {
        val items = listOf(Dashboard, Notes, Customization, Settings)
    }
}

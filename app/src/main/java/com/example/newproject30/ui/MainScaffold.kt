package com.example.newproject30.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.newproject30.ui.addentry.AddEntryBottomSheet
import com.example.newproject30.ui.customization.CustomizationScreen
import com.example.newproject30.ui.dashboard.DashboardScreen
import com.example.newproject30.ui.detail.EntryDetailScreen
import com.example.newproject30.ui.navigation.BottomNavItem
import com.example.newproject30.ui.navigation.CustomizationRoute
import com.example.newproject30.ui.navigation.DashboardRoute
import com.example.newproject30.ui.navigation.EntryDetailRoute
import com.example.newproject30.ui.navigation.NotesRoute
import com.example.newproject30.ui.navigation.SettingsRoute
import com.example.newproject30.ui.notes.NotesScreen
import com.example.newproject30.ui.settings.SettingsScreen

/**
 * Main application navigation shell with Material 3 NavigationBar, FAB, and Add Entry ModalBottomSheet.
 *
 * @param modifier Modifier for root container layout styling.
 * @param navController Navigation controller managing backstack and routes.
 */
@Composable
fun MainScaffold(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    var showAddEntrySheet by rememberSaveable { mutableStateOf(false) }

    val onNavigateToDestination: (BottomNavItem) -> Unit = { item ->
        navController.navigate(item.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                BottomNavItem.items.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any {
                        it.hasRoute(item.route::class)
                    } == true

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onNavigateToDestination(item) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(text = item.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEntrySheet = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "Voice Record / Quick Entry"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(180)) },
            exitTransition = { fadeOut(animationSpec = tween(180)) },
            popEnterTransition = { fadeIn(animationSpec = tween(180)) },
            popExitTransition = { fadeOut(animationSpec = tween(180)) }
        ) {
            composable<DashboardRoute> {
                DashboardScreen(
                    onNavigateToNotes = {
                        navController.navigate(NotesRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onEntryClick = { entryId ->
                        navController.navigate(EntryDetailRoute(entryId))
                    }
                )
            }
            composable<NotesRoute> {
                NotesScreen(
                    onEntryClick = { entryId ->
                        navController.navigate(EntryDetailRoute(entryId))
                    }
                )
            }
            composable<CustomizationRoute> {
                CustomizationScreen()
            }
            composable<SettingsRoute> {
                SettingsScreen()
            }
            composable<EntryDetailRoute> { backStackEntry ->
                val route: EntryDetailRoute = backStackEntry.toRoute()
                EntryDetailScreen(
                    entryId = route.entryId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // Display Add Entry / Voice Note Modal Bottom Sheet when FAB is clicked
        if (showAddEntrySheet) {
            AddEntryBottomSheet(
                onDismiss = { showAddEntrySheet = false }
            )
        }
    }
}

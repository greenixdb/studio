package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppHeader
import com.example.ui.components.AppNavigationDrawer
import com.example.ui.components.AppTab
import com.example.ui.components.BottomNavBar
import com.example.ui.components.ConnectDatabaseModal
import com.example.ui.components.CreateDatabaseModal
import com.example.ui.components.DatabaseQueryConsoleModal
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.ProfileDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyDatabasesScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.GreenixStudioTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GreenixViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GreenixStudioApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreenixStudioApp(
    viewModel: GreenixViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val databases by viewModel.databases.collectAsStateWithLifecycle()

    val showCreateModal by viewModel.showCreateModal.collectAsStateWithLifecycle()
    val showConnectModal by viewModel.showConnectModal.collectAsStateWithLifecycle()
    val showNotificationsSheet by viewModel.showNotificationsSheet.collectAsStateWithLifecycle()
    val showProfileDialog by viewModel.showProfileDialog.collectAsStateWithLifecycle()
    val activeConsoleDatabase by viewModel.activeConsoleDatabase.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    GreenixStudioTheme(darkTheme = isDarkTheme) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                AppScreen.SPLASH -> {
                    SplashScreen(
                        onSplashFinished = { viewModel.navigateToAuth() }
                    )
                }

                AppScreen.AUTH -> {
                    AuthScreen(
                        onLoginSuccess = { name, email ->
                            viewModel.handleLogin(name, email)
                        }
                    )
                }

                AppScreen.MAIN -> {
                    AppNavigationDrawer(
                        drawerState = drawerState,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = { isDark -> viewModel.toggleDarkTheme(isDark) },
                        userName = userName,
                        userEmail = userEmail,
                        selectedTab = selectedTab,
                        onSelectTab = { tab ->
                            viewModel.selectTab(tab)
                            coroutineScope.launch { drawerState.close() }
                        },
                        onCreateDbClick = {
                            viewModel.openCreateModal()
                            coroutineScope.launch { drawerState.close() }
                        },
                        onConnectDbClick = {
                            viewModel.openConnectModal()
                            coroutineScope.launch { drawerState.close() }
                        },
                        onAboutClick = {
                            viewModel.openProfileDialog()
                            coroutineScope.launch { drawerState.close() }
                        },
                        onSignOutClick = {
                            viewModel.handleSignOut()
                            coroutineScope.launch { drawerState.close() }
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                AppHeader(
                                    onMenuClick = {
                                        coroutineScope.launch {
                                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                        }
                                    },
                                    onNotificationsClick = { viewModel.openNotificationsSheet() },
                                    onProfileClick = { viewModel.openProfileDialog() },
                                    unreadNotificationCount = 4,
                                    userInitial = userName.take(1)
                                )
                            },
                            bottomBar = {
                                BottomNavBar(
                                    selectedTab = selectedTab,
                                    onTabSelected = { tab -> viewModel.selectTab(tab) }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                Crossfade(targetState = selectedTab, label = "TabTransition") { tab ->
                                    when (tab) {
                                        AppTab.HOME -> {
                                            HomeScreen(
                                                userName = userName,
                                                databases = databases,
                                                onCreateDatabaseClick = { viewModel.openCreateModal() },
                                                onConnectDatabaseClick = { viewModel.openConnectModal() },
                                                onOpenQueryConsole = { db -> viewModel.openQueryConsole(db) },
                                                onViewAllDatabasesClick = { viewModel.selectTab(AppTab.MY_DATABASES) }
                                            )
                                        }

                                        AppTab.MY_DATABASES -> {
                                            MyDatabasesScreen(
                                                databases = databases,
                                                onCreateDatabaseClick = { viewModel.openCreateModal() },
                                                onConnectDatabaseClick = { viewModel.openConnectModal() },
                                                onOpenQueryConsole = { db -> viewModel.openQueryConsole(db) },
                                                onDeleteDatabase = { db -> viewModel.deleteDatabase(db) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Modals & Bottom Sheets
        if (showCreateModal) {
            CreateDatabaseModal(
                onDismiss = { viewModel.closeCreateModal() },
                onCreateDatabase = { name, engine, dbName ->
                    viewModel.createDatabase(name, engine, dbName)
                }
            )
        }

        if (showConnectModal) {
            ConnectDatabaseModal(
                onDismiss = { viewModel.closeConnectModal() },
                onConnectDatabase = { name, engine, host, port, dbName, username ->
                    viewModel.connectDatabase(name, engine, host, port, dbName, username)
                }
            )
        }

        if (showNotificationsSheet) {
            NotificationsSheet(
                onDismiss = { viewModel.closeNotificationsSheet() }
            )
        }

        if (showProfileDialog) {
            ProfileDialog(
                userName = userName,
                userEmail = userEmail,
                onDismiss = { viewModel.closeProfileDialog() },
                onSignOut = { viewModel.handleSignOut() }
            )
        }

        activeConsoleDatabase?.let { db ->
            DatabaseQueryConsoleModal(
                database = db,
                onDismiss = { viewModel.closeQueryConsole() }
            )
        }
    }
}

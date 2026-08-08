package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.GreenixDatabase
import com.example.data.model.DatabaseEntity
import com.example.data.repository.DatabaseRepository
import com.example.ui.components.AppTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
    AUTH,
    MAIN
}

class GreenixViewModel(application: Application) : AndroidViewModel(application) {

    private val databaseRepository: DatabaseRepository

    init {
        val database = GreenixDatabase.getDatabase(application, viewModelScope)
        databaseRepository = DatabaseRepository(database.databaseDao())
    }

    val databases: StateFlow<List<DatabaseEntity>> = databaseRepository.allDatabases
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedTab = MutableStateFlow(AppTab.HOME)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    // Dark Mode Theme Override (DEFAULT IS DARK MODE = true)
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Auth State
    private val _userName = MutableStateFlow("Admin Edgicode")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("admin@edgicode.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    // Modal UI states
    private val _showCreateModal = MutableStateFlow(false)
    val showCreateModal: StateFlow<Boolean> = _showCreateModal.asStateFlow()

    private val _showConnectModal = MutableStateFlow(false)
    val showConnectModal: StateFlow<Boolean> = _showConnectModal.asStateFlow()

    private val _showNotificationsSheet = MutableStateFlow(false)
    val showNotificationsSheet: StateFlow<Boolean> = _showNotificationsSheet.asStateFlow()

    private val _showProfileDialog = MutableStateFlow(false)
    val showProfileDialog: StateFlow<Boolean> = _showProfileDialog.asStateFlow()

    private val _activeConsoleDatabase = MutableStateFlow<DatabaseEntity?>(null)
    val activeConsoleDatabase: StateFlow<DatabaseEntity?> = _activeConsoleDatabase.asStateFlow()

    fun navigateToAuth() {
        _currentScreen.value = AppScreen.AUTH
    }

    fun handleLogin(name: String, email: String) {
        _userName.value = name
        _userEmail.value = email
        _currentScreen.value = AppScreen.MAIN
    }

    fun handleSignOut() {
        _currentScreen.value = AppScreen.AUTH
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun toggleDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    fun openCreateModal() {
        _showCreateModal.value = true
    }

    fun closeCreateModal() {
        _showCreateModal.value = false
    }

    fun openConnectModal() {
        _showConnectModal.value = true
    }

    fun closeConnectModal() {
        _showConnectModal.value = false
    }

    fun openNotificationsSheet() {
        _showNotificationsSheet.value = true
    }

    fun closeNotificationsSheet() {
        _showNotificationsSheet.value = false
    }

    fun openProfileDialog() {
        _showProfileDialog.value = true
    }

    fun closeProfileDialog() {
        _showProfileDialog.value = false
    }

    fun openQueryConsole(database: DatabaseEntity) {
        _activeConsoleDatabase.value = database
    }

    fun closeQueryConsole() {
        _activeConsoleDatabase.value = null
    }

    fun createDatabase(name: String, engine: String, dbName: String) {
        viewModelScope.launch {
            databaseRepository.createDatabase(
                name = name,
                engine = engine,
                dbName = dbName,
                username = _userName.value.lowercase().replace(" ", "_")
            )
        }
    }

    fun connectDatabase(name: String, engine: String, host: String, port: Int, dbName: String, username: String) {
        viewModelScope.launch {
            databaseRepository.connectDatabase(
                name = name,
                engine = engine,
                host = host,
                port = port,
                dbName = dbName,
                username = username
            )
        }
    }

    fun deleteDatabase(database: DatabaseEntity) {
        viewModelScope.launch {
            databaseRepository.deleteDatabase(database)
        }
    }
}

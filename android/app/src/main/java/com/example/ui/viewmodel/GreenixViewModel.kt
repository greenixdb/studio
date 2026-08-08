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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
    AUTH,
    MAIN,
    CREATE_DATABASE,
    CONNECT_DATABASE,
    DATABASE_DASHBOARD
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

    // Active database powering the dashboard screen
    private val _activeDatabase = MutableStateFlow<DatabaseEntity?>(null)
    val activeDatabase: StateFlow<DatabaseEntity?> = _activeDatabase.asStateFlow()

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

    fun openCreateDatabaseScreen() {
        _currentScreen.value = AppScreen.CREATE_DATABASE
    }

    fun openConnectDatabaseScreen() {
        _currentScreen.value = AppScreen.CONNECT_DATABASE
    }

    fun openDatabaseDashboard(database: DatabaseEntity) {
        _activeDatabase.value = database
        _currentScreen.value = AppScreen.DATABASE_DASHBOARD
    }

    fun backToMain() {
        _activeDatabase.value = null
        _currentScreen.value = AppScreen.MAIN
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

    fun createDatabase(category: String, engine: String, name: String) {
        viewModelScope.launch {
            val id = databaseRepository.createDatabase(
                name = name,
                engine = engine,
                dbName = name.lowercase().replace(" ", "_"),
                category = category,
                port = defaultPortFor(engine),
                username = _userName.value.lowercase().replace(" ", "_")
            )
            openDatabaseById(id)
        }
    }

    fun connectDatabase(connectionString: String) {
        viewModelScope.launch {
            val parsed = parseConnectionString(connectionString)
            val id = databaseRepository.connectDatabase(
                name = parsed.dbName.ifBlank { "Connected Database" },
                engine = parsed.engine,
                category = categoryFor(parsed.engine),
                host = parsed.host,
                port = parsed.port,
                dbName = parsed.dbName,
                username = parsed.username
            )
            openDatabaseById(id)
        }
    }

    private suspend fun openDatabaseById(id: Long) {
        val database = databaseRepository.getDatabaseById(id).first()
        if (database != null) {
            openDatabaseDashboard(database)
        } else {
            backToMain()
        }
    }

    fun deleteDatabase(database: DatabaseEntity) {
        viewModelScope.launch {
            databaseRepository.deleteDatabase(database)
            if (_activeDatabase.value?.id == database.id) {
                backToMain()
            }
        }
    }

    private data class ParsedConnection(
        val engine: String,
        val host: String,
        val port: Int,
        val dbName: String,
        val username: String
    )

    private fun parseConnectionString(raw: String): ParsedConnection {
        val value = raw.trim()
        val scheme = value.substringBefore("://", "").lowercase()
        val engine = when {
            scheme.startsWith("postgres") -> "PostgreSQL"
            scheme.startsWith("mysql") -> "MySQL"
            scheme.startsWith("mongodb") -> "MongoDB"
            scheme.startsWith("redis") -> "Redis"
            scheme.startsWith("neo4j") || scheme.startsWith("bolt") -> "Neo4j"
            scheme.startsWith("sqlite") || scheme.startsWith("file") -> "SQLite"
            else -> "PostgreSQL"
        }
        val rest = value.substringAfter("://", value)
        val credentials = if (rest.contains("@")) rest.substringBefore("@") else ""
        val hostAndPath = if (rest.contains("@")) rest.substringAfter("@") else rest
        val hostPort = hostAndPath.substringBefore("/")
        val host = hostPort.substringBefore(":").ifBlank { "localhost" }
        val port = hostPort.substringAfter(":", "").substringBefore("?")
            .toIntOrNull() ?: defaultPortFor(engine)
        val dbName = hostAndPath.substringAfter("/", "").substringBefore("?")
        val username = credentials.substringBefore(":").ifBlank { "admin" }
        return ParsedConnection(engine, host, port, dbName, username)
    }

    private fun defaultPortFor(engine: String): Int = when (engine) {
        "PostgreSQL" -> 5432
        "MySQL" -> 3306
        "MongoDB" -> 27017
        "Redis" -> 6379
        "Neo4j" -> 7687
        "ArangoDB" -> 8529
        else -> 0
    }

    private fun categoryFor(engine: String): String = when (engine) {
        "MongoDB", "Redis" -> "No SQL"
        "Neo4j", "ArangoDB" -> "Graph"
        else -> "Relational"
    }
}

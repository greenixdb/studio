package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EmeraldDarkContainer
import com.example.ui.theme.EmeraldPrimary

enum class AppTab {
    HOME,
    MY_DATABASES
}

@Composable
fun BottomNavBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        // HOME TAB
        NavigationBarItem(
            selected = selectedTab == AppTab.HOME,
            onClick = { onTabSelected(AppTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home Tab"
                )
            },
            label = {
                Text(
                    text = "Home",
                    fontWeight = if (selectedTab == AppTab.HOME) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                selectedTextColor = EmeraldPrimary,
                indicatorColor = EmeraldPrimary.copy(alpha = 0.18f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // MY DATABASES TAB
        NavigationBarItem(
            selected = selectedTab == AppTab.MY_DATABASES,
            onClick = { onTabSelected(AppTab.MY_DATABASES) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == AppTab.MY_DATABASES) Icons.Filled.Storage else Icons.Outlined.Storage,
                    contentDescription = "My Databases Tab"
                )
            },
            label = {
                Text(
                    text = "My Databases",
                    fontWeight = if (selectedTab == AppTab.MY_DATABASES) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EmeraldPrimary,
                selectedTextColor = EmeraldPrimary,
                indicatorColor = EmeraldPrimary.copy(alpha = 0.18f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

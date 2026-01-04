package com.example.bookexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.bookexplorer.ui.BookApp
import com.example.bookexplorer.ui.theme.BookExplorerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: com.example.bookexplorer.ui.viewmodel.ThemeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val isDarkModePreference by themeViewModel.isDarkMode.collectAsState(initial = null)
            val systemDark = isSystemInDarkTheme()
            
            val darkTheme = isDarkModePreference ?: systemDark

            BookExplorerTheme(
                darkTheme = darkTheme
            ) {
                BookApp(
                    isDarkTheme = darkTheme,
                    onToggleTheme = { themeViewModel.toggleTheme(darkTheme) }
                )
            }
        }
    }
}
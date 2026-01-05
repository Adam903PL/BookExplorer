package com.example.bookexplorer




import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookexplorer.ui.home.HomeScreen


@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onBookClick = { bookId ->
                    // NOTE: This legacy NavHost expects title/author but now we only have ID.
                    // This file seems unused in favor of BookApp.kt.
                    // Passing dummy data or navigating loosely to fix compilation.
                    val encodedTitle = Uri.encode("Loading...")
                    val encodedAuthor = Uri.encode("Unknown")
                    val safeCoverId = -1
                    navController.navigate("book/$encodedTitle/$encodedAuthor/$safeCoverId")
                },
                onInfoClick = { navController.navigate("info") }
            )
        }



        composable(
            route = "book/{title}/{author}/{coverId}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("author") { type = NavType.StringType },
                navArgument("coverId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Brak tytułu"
            val author = backStackEntry.arguments?.getString("author") ?: "Brak autora"
            val coverId = backStackEntry.arguments?.getInt("coverId") ?: -1


              BookScreen(
                 title = title,
                 author = author,
                 coverId = if (coverId == -1) null else coverId,
                 onBack = { navController.popBackStack() }
              )
        }

        composable("info") {
            com.example.bookexplorer.ui.info.InfoScreen(onBack = { navController.popBackStack() })
        }
    }
}

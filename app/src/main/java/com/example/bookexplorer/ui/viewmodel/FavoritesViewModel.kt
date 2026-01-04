package com.example.bookexplorer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookexplorer.data.BookDetail
import com.example.bookexplorer.data.BookRepository
import com.example.bookexplorer.data.FavoritesManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class FavoritesUiState {
    object Loading : FavoritesUiState()
    data class Success(val favorites: List<Pair<String, BookDetail>>) : FavoritesUiState()
    object Empty : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BookRepository()
    private val favoritesManager = FavoritesManager(application.applicationContext)

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = FavoritesUiState.Loading
            // Get single snapshot for loading list. 
            // Real-time updates could be done by collecting the flow, but simple load is okay for now.
            val favoriteIds = favoritesManager.favoritesFlow.first()
            
            if (favoriteIds.isEmpty()) {
                _uiState.value = FavoritesUiState.Empty
                return@launch
            }

            val deferredDetails = favoriteIds.map { id ->
                async { 
                    id to repository.getBookDetails(id) 
                }
            }
            
            val results = deferredDetails.awaitAll()
            
            val validFavorites = results.mapNotNull { (id, result) ->
                result.getOrNull()?.let { id to it }
            }

            if (validFavorites.isEmpty() && favoriteIds.isNotEmpty()) {
                 _uiState.value = FavoritesUiState.Error("Failed to load favorites")
            } else {
                _uiState.value = FavoritesUiState.Success(validFavorites)
            }
        }
    }
}

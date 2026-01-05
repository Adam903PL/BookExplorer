package com.example.bookexplorer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bookexplorer.data.BookDetail
import com.example.bookexplorer.data.BookRepository
import com.example.bookexplorer.data.FavoritesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val book: BookDetail) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

class BookDetailViewModel(application: Application, private val workId: String) : AndroidViewModel(application) {
    private val repository = BookRepository()
    private val favoritesManager = FavoritesManager(application.applicationContext)
    
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState

    val isFavorite: StateFlow<Boolean> = favoritesManager.favoritesFlow
        .map { it.contains(workId) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        loadBookDetails()
    }

    fun loadBookDetails() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val result = repository.getBookDetails(workId)
            result.onSuccess { book ->
                _uiState.value = DetailUiState.Success(book)
            }.onFailure { e ->
                _uiState.value = DetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {

            if (isFavorite.value) {
                favoritesManager.removeFavorite(workId)
            } else {
                favoritesManager.addFavorite(workId)
            }
        }
    }
}

class BookDetailViewModelFactory(private val application: Application, private val workId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookDetailViewModel(application, workId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

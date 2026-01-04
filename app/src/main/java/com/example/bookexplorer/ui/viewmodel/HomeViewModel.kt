package com.example.bookexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookexplorer.data.BookRepository
import com.example.bookexplorer.data.BookWork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val books: List<BookWork>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {
    private val repository = BookRepository()
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    val isRefreshing: StateFlow<Boolean> = _uiState.map { it is HomeUiState.Loading && (it as? HomeUiState.Success)?.books?.isEmpty() == true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var currentOffset = 0
    private var isLastPage = false
    private var isLoadingMore = false

    init {
        loadBooks(reset = true)
    }

    fun loadBooks(reset: Boolean = false) {
        if (isLoadingMore) return

        if (reset) {
            currentOffset = 0
            isLastPage = false
            _uiState.value = HomeUiState.Loading
        }

        if (isLastPage) return

        isLoadingMore = true
        viewModelScope.launch {
            val result = repository.getFictionBooks(offset = currentOffset)
            result.onSuccess { books ->
                if (books.isEmpty()) {
                    isLastPage = true
                } else {
                    currentOffset += books.size
                    val currentBooks = (_uiState.value as? HomeUiState.Success)?.books ?: emptyList()
                    val newBooks = if (reset) books else currentBooks + books
                    _uiState.value = HomeUiState.Success(newBooks)
                }
            }.onFailure { e ->
                if (reset) {
                    _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
                }
                // Handle pagination error silently or with a snackbar side-effect if needed
            }
            isLoadingMore = false
        }
    }

    fun loadNextPage() {
        loadBooks(reset = false)
    }
}

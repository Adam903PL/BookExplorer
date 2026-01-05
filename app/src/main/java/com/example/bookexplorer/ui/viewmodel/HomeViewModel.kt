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

enum class SortOption {
    TITLE, YEAR
}

class HomeViewModel : ViewModel() {
    private val repository = BookRepository()
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val _books = MutableStateFlow<List<BookWork>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _yearQuery = MutableStateFlow("")
    val yearQuery: StateFlow<String> = _yearQuery

    private val _sortOption = MutableStateFlow(SortOption.TITLE)
    val sortOption: StateFlow<SortOption> = _sortOption

    val uiState: StateFlow<HomeUiState> = kotlinx.coroutines.flow.combine(_books, _searchQuery, _yearQuery, _sortOption, _uiState) { books, query, year, sort, state ->
        if (state is HomeUiState.Loading && books.isEmpty()) {
            HomeUiState.Loading
        } else if (state is HomeUiState.Error && books.isEmpty()) {
            state
        } else {
            val filteredBooks = books.filter { book ->
                val matchesTitle = book.title.contains(query, ignoreCase = true)
                val matchesYear = if (year.isNotEmpty()) {
                     book.firstPublishYear?.toString() == year
                } else true
                matchesTitle && matchesYear
            }
            val sortedBooks = when (sort) {
                SortOption.TITLE -> filteredBooks.sortedBy { it.title }
                SortOption.YEAR -> filteredBooks.sortedBy { it.firstPublishYear ?: Int.MAX_VALUE }
            }
            HomeUiState.Success(sortedBooks)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState.Loading)

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
                    val currentBooks = _books.value
                    val newBooks = if (reset) books else currentBooks + books
                    _books.value = newBooks
                    if (_uiState.value is HomeUiState.Loading || _uiState.value is HomeUiState.Error) {
                         _uiState.value = HomeUiState.Success(newBooks) 
                    }
                }
            }.onFailure { e ->
                if (reset) {
                    _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
                }
            }
            isLoadingMore = false
        }
    }

    fun loadNextPage() {
        loadBooks(reset = false)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onYearQueryChanged(year: String) {
        _yearQuery.value = year
    }

    fun onSortOptionChanged(option: SortOption) {
        _sortOption.value = option
    }
}

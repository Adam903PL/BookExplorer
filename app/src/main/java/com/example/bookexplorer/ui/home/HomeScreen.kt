package com.example.bookexplorer.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bookexplorer.data.BookWork
import com.example.bookexplorer.ui.viewmodel.HomeUiState
import com.example.bookexplorer.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onBookClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.loadBooks(reset = true) }
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .pullRefresh(pullRefreshState)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = viewModel.searchQuery.collectAsState().value,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search title...") },
                        leadingIcon = { 
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Search, 
                                contentDescription = "Search"
                            ) 
                        },
                        singleLine = true,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.OutlinedTextField(
                            value = viewModel.yearQuery.collectAsState().value,
                            onValueChange = { viewModel.onYearQueryChanged(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Year (e.g. 1890)") },
                            singleLine = true,
                            leadingIcon = {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.DateRange,
                                    contentDescription = "Year"
                                )
                            },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                disabledContainerColor = MaterialTheme.colorScheme.surface,
                            )
                        )
                    }
                }
            }


            when (val state = uiState) {
                is HomeUiState.Loading -> {
                     if (!isRefreshing && (state as? HomeUiState.Loading)?.let { true } == true) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                     }
                }
                is HomeUiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(state.books) { index, book ->
                            if (index >= state.books.size - 1) {
                                LaunchedEffect(Unit) {
                                    viewModel.loadNextPage()
                                }
                            }
                            BookListItem(book = book, onClick = { 
                                val bookId = book.key.substringAfterLast("/")
                                onBookClick(bookId) 
                            })
                        }
                    }
                }
                is HomeUiState.Error -> {
                    if (!isRefreshing) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                                Button(onClick = { viewModel.loadBooks(reset = true) }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }
        }
        
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun BookListItem(book: BookWork, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val coverUrl = book.coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(coverUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Book Cover",
            modifier = Modifier.size(60.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            book.authors?.let { authors ->
                 Text(
                    text = authors.joinToString { it.name },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                     overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

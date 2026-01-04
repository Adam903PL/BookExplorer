package com.example.bookexplorer.data

import com.example.bookexplorer.core.NetworkModule
import java.io.IOException

class BookRepository {
    private val api = NetworkModule.api

    suspend fun getFictionBooks(limit: Int = 20, offset: Int = 0): Result<List<BookWork>> {
        return try {
            val response = api.getFictionBooks(limit, offset)
            Result.success(response.works)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBookDetails(workId: String): Result<BookDetail> {
        return try {
            val response = api.getBookDetails(workId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchBooks(query: String): Result<List<SearchDoc>> {
        return try {
            val response = api.searchBooks(query)
            Result.success(response.docs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

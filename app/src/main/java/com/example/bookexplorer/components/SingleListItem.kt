package com.example.bookexplorer.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SingleListItem (
    title: String = "Tytuł elementu",
    description: String = "Opis elementu z dodatkowymi informacjami",
) {
    Row() {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Ikona ucarlosci",
            modifier = Modifier.width(80.dp).height(80.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column() {
            Text(text = title, style = MaterialTheme.typography.headlineLarge,fontWeight = FontWeight.Bold,)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = description,style = MaterialTheme.typography.headlineSmall)
        }
    }
}
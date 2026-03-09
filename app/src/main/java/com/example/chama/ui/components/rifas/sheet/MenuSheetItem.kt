package com.example.chama.ui.components.rifas.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MenuSheetItem(titulo: String, subtitulo: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitulo, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
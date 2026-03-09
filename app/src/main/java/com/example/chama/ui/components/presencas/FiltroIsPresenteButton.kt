package com.example.chama.ui.components.presencas

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun FiltroBtn(label: String, icone: ImageVector?, selecionado: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = 12.dp,
            vertical = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = Color.Black
        ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icone != null){
                Icon(
                    imageVector = icone,
                    contentDescription = "Presentes",
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(label)
        }
    }
}
package com.example.chama.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import com.example.chama.data.Crismando


@Composable
fun CrismandoCard (crismando: Crismando, selecionado: Boolean, onClick: () -> Unit) {

    val corFundo by animateColorAsState(
        targetValue = if (selecionado)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        label = "AnimacaoCor"
    )

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    .clickable{ onClick() },
    colors = CardDefaults.cardColors(containerColor = corFundo),
    elevation = CardDefaults.cardElevation(
    defaultElevation = if (selecionado) 6.dp else 1.dp
    )) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = crismando.nome,
                modifier = Modifier.padding(16.dp)
                    .weight(1f),
                style = if (selecionado)
                    MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                else
                    MaterialTheme.typography.bodyLarge)

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Presença Confirmada",
                modifier = Modifier.padding(horizontal = 16.dp),
                tint = if (selecionado)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        }
    }

}
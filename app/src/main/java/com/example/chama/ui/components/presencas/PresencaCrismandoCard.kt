package com.example.chama.ui.components.presencas

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.chama.data.entity.Crismando

@Composable
fun CrismandoCard(
    crismando: Crismando,
    estaPresente: Boolean?,
    selecionado: Boolean,
    onClick: () -> Unit,
    onInfoClick: (() -> Unit)? = null
) {
    val corFundo by animateColorAsState(
        targetValue = if (estaPresente == true)
            Color(0x4A15981C)
        else if (estaPresente == false)
            Color(0x4A9B0A0A)
        else
            MaterialTheme.colorScheme.surfaceVariant,
        label = "AnimacaoCor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = corFundo),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selecionado) 6.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar / Foto do Crismando (agora clicável)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .then(
                        if (onInfoClick != null) {
                            Modifier.clickable { onInfoClick() }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!crismando.fotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = crismando.fotoUrl,
                        contentDescription = "Foto de ${crismando.nome}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = crismando.nome,
                modifier = Modifier.weight(1f),
                style = if (selecionado)
                    MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                else
                    MaterialTheme.typography.bodyLarge
            )

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Status Selecionado",
                modifier = Modifier.padding(start = 4.dp, end = 4.dp),
                tint = if (selecionado)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        }
    }
}
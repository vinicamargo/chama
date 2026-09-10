package com.example.chama.ui.components.common.detalhescrismando.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chama.ui.components.common.detalhescrismando.BlocoItemUI

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SecaoRifasVinculadas(
    blocosVinculados: List<BlocoItemUI>,
    corDestaque: Color,
    isExpandido: Boolean,
    onToggleExpandir: () -> Unit,
    onAbrirVincular: () -> Unit,
    onClicarBloco: (BlocoItemUI) -> Unit,
    modifier: Modifier = Modifier
) {
    SecaoColapsavelCard(
        titulo = "Rifas Vinculadas",
        icone = Icons.Default.ConfirmationNumber,
        corDestaque = corDestaque,
        isExpandido = isExpandido,
        onToggleExpandir = onToggleExpandir,
        modifier = modifier,
        trailingBadge = {
            Text(
                text = "${blocosVinculados.size} bloco(s)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = corDestaque,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (blocosVinculados.isNotEmpty())
                                "${blocosVinculados.size} bloco(s) em posse"
                            else
                                "Nenhum bloco vinculado",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    TextButton(onClick = onAbrirVincular) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = corDestaque)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Vincular", color = corDestaque, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (blocosVinculados.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        blocosVinculados.forEach { blocoItem ->
                            val inicio = (blocoItem.numero - 1) * 10 + 1
                            val fim = inicio + 9
                            val labelTexto = if (blocoItem.estaPago) {
                                "Bloco ${blocoItem.numero} ($inicio-$fim) • Pago"
                            } else {
                                "Bloco ${blocoItem.numero} ($inicio-$fim)"
                            }

                            InputChip(
                                selected = blocoItem.estaPago,
                                onClick = { onClicarBloco(blocoItem) },
                                label = {
                                    Text(
                                        text = labelTexto,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (blocoItem.estaPago) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    containerColor = corDestaque.copy(alpha = 0.15f),
                                    selectedContainerColor = Color(0xFF2E7D32).copy(alpha = 0.18f),
                                    selectedLabelColor = Color(0xFF1B5E20)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
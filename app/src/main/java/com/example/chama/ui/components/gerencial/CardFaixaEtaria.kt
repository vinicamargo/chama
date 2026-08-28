package com.example.chama.ui.components.gerencial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.chama.data.entity.Crismando
import java.time.LocalDate
import java.time.Period

data class CrismandoComIdade(
    val crismando: Crismando,
    val idadeExata: Int
)

@Composable
fun CardFaixaEtaria(
    crismandos: List<Crismando>,
    onCrismandoClick: (Crismando) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coresFaixas = listOf(
        Color(0xFF1E88E5),
        Color(0xFF43A047),
        Color(0xFFFB8C00),
        Color(0xFF8E24AA),
        Color(0xFFE53935),
        Color(0xFF00ACC1)
    )

    // Estado da faixa selecionada para exibir os crismandos
    var faixaSelecionada by remember { mutableStateOf<String?>(null) }

    // Agrupamento dos crismandos com idade calculada
    val gruposPorFaixa = remember(crismandos) {
        val mapa = linkedMapOf<String, MutableList<CrismandoComIdade>>()
        val faixasBase = listOf("13 - 15 anos", "16 - 17 anos", "18 - 20 anos", "20 - 25 anos")
        faixasBase.forEach { mapa[it] = mutableListOf() }

        val crismandos26Mais = mutableListOf<CrismandoComIdade>()

        for (c in crismandos) {
            val idade = runCatching {
                c.dataNascimento?.let {
                    val birthDate = LocalDate.parse(it)
                    Period.between(birthDate, LocalDate.now()).years
                }
            }.getOrNull() ?: c.idade

            if (idade != null) {
                val item = CrismandoComIdade(c, idade)
                when (idade) {
                    in 13..15 -> mapa["13 - 15 anos"]?.add(item)
                    in 16..17 -> mapa["16 - 17 anos"]?.add(item)
                    in 18..20 -> mapa["18 - 20 anos"]?.add(item)
                    in 20..25 -> mapa["20 - 25 anos"]?.add(item)
                    else -> if (idade >= 26) crismandos26Mais.add(item)
                }
            }
        }

        if (crismandos26Mais.isNotEmpty()) {
            mapa["26+ anos"] = crismandos26Mais
        }

        mapa
    }

    // Fatias ativas para o gráfico
    val fatias = remember(gruposPorFaixa) {
        gruposPorFaixa.entries
            .filter { it.value.isNotEmpty() }
            .mapIndexed { index, entry ->
                Triple(entry.key, entry.value.size, coresFaixas[index % coresFaixas.size])
            }
    }

    val totalCrismandosComIdade = remember(gruposPorFaixa) {
        gruposPorFaixa.values.sumOf { it.size }
    }

    // Animação do gráfico de rosca
    val progressAnim = remember { Animatable(0f) }
    LaunchedEffect(fatias) {
        progressAnim.snapTo(0f)
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Distribuição por Faixa Etária",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (faixaSelecionada != null) {
                    Text(
                        text = "Limpar filtro",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { faixaSelecionada = null }
                            .padding(4.dp)
                    )
                }
            }

            if (totalCrismandosComIdade == 0 || fatias.isEmpty()) {
                Text(
                    text = "Sem dados de idade cadastrados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Gráfico de Pizza / Donut
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(130.dp)
                    ) {
                        Canvas(modifier = Modifier.size(110.dp)) {
                            var startAngle = -90f
                            val strokeWidthNormal = 20.dp.toPx()
                            val strokeWidthSelecionado = 26.dp.toPx()

                            for ((faixa, qtd, cor) in fatias) {
                                val isSelecionado = faixaSelecionada == faixa
                                val sweepAngle = (qtd.toFloat() / totalCrismandosComIdade.toFloat()) * 360f * progressAnim.value

                                drawArc(
                                    color = if (faixaSelecionada == null || isSelecionado) cor else cor.copy(alpha = 0.25f),
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(
                                        width = if (isSelecionado) strokeWidthSelecionado else strokeWidthNormal,
                                        cap = StrokeCap.Butt
                                    )
                                )
                                startAngle += sweepAngle
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${faixaSelecionada?.let { gruposPorFaixa[it]?.size } ?: totalCrismandosComIdade}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (faixaSelecionada != null) "na faixa" else "total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Legenda Clicável
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        fatias.forEach { (faixa, qtd, cor) ->
                            val perc = (qtd.toFloat() / totalCrismandosComIdade.toFloat()) * 100f
                            val selecionado = faixaSelecionada == faixa

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selecionado) cor.copy(alpha = 0.15f) else Color.Transparent
                                    )
                                    .clickable {
                                        faixaSelecionada = if (faixaSelecionada == faixa) null else faixa
                                    }
                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(cor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = faixa,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selecionado) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }

                                Text(
                                    text = "(%.0f%%)".format(perc),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selecionado) cor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Lista de Crismandos da Faixa Selecionada
                AnimatedVisibility(
                    visible = faixaSelecionada != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    val listaNaFaixa = faixaSelecionada?.let { gruposPorFaixa[it] } ?: emptyList()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Text(
                            text = "Crismandos entre $faixaSelecionada:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        listaNaFaixa.sortedBy { it.idadeExata }.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onCrismandoClick(item.crismando) }
                                    .padding(vertical = 6.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!item.crismando.fotoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = item.crismando.fotoUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = item.crismando.nome,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = "${item.idadeExata} anos",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Ver detalhes",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
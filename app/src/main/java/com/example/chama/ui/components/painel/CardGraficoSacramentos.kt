package com.example.chama.ui.components.painel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

sealed class TipoSacramentoGrafico(val titulo: String) {
    object Batismo : TipoSacramentoGrafico("Batismo")
    object Certidao : TipoSacramentoGrafico("Certidão")
    object Comunhao : TipoSacramentoGrafico("1ª Comunhão")
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Composable
fun CardGraficoSacramentos(
    totalCrismandos: Int,
    totalBatizados: Int,
    totalCertidaoEntregue: Int,
    totalPrimeiraComunhao: Int,
    modifier: Modifier = Modifier,
    corDestaque: Color = Color(0xFF9B8800)
) {
    var abaSelecionada by remember { mutableStateOf<TipoSacramentoGrafico>(TipoSacramentoGrafico.Batismo) }

    val (quantidadeSim, quantidadeNao, rotuloSim, rotuloNao) = when (abaSelecionada) {
        is TipoSacramentoGrafico.Batismo -> {
            val s = totalBatizados
            val n = (totalCrismandos - s).coerceAtLeast(0)
            Quad(s, n, "Batizados", "Não Batizados")
        }
        is TipoSacramentoGrafico.Certidao -> {
            val s = totalCertidaoEntregue
            val n = (totalCrismandos - s).coerceAtLeast(0)
            Quad(s, n, "Entregue", "Pendente")
        }
        is TipoSacramentoGrafico.Comunhao -> {
            val s = totalPrimeiraComunhao
            val n = (totalCrismandos - s).coerceAtLeast(0)
            Quad(s, n, "Possuem", "Não Possuem")
        }
    }

    val porcentagemSim = if (totalCrismandos > 0) (quantidadeSim.toFloat() / totalCrismandos) * 100f else 0f
    val porcentagemNao = if (totalCrismandos > 0) (quantidadeNao.toFloat() / totalCrismandos) * 100f else 0f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabeçalho e Seletor (Abas)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Vida Sacramental",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Linha de Botões de Alternância (Filtros)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiltroBotaoTab(
                        texto = "Batismo",
                        selecionado = abaSelecionada is TipoSacramentoGrafico.Batismo,
                        corDestaque = corDestaque,
                        modifier = Modifier.weight(1f),
                        onClick = { abaSelecionada = TipoSacramentoGrafico.Batismo }
                    )
                    FiltroBotaoTab(
                        texto = "Certidão",
                        selecionado = abaSelecionada is TipoSacramentoGrafico.Certidao,
                        corDestaque = corDestaque,
                        modifier = Modifier.weight(1f),
                        onClick = { abaSelecionada = TipoSacramentoGrafico.Certidao }
                    )
                    FiltroBotaoTab(
                        texto = "Comunhão",
                        selecionado = abaSelecionada is TipoSacramentoGrafico.Comunhao,
                        corDestaque = corDestaque,
                        modifier = Modifier.weight(1f),
                        onClick = { abaSelecionada = TipoSacramentoGrafico.Comunhao }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Conteúdo do Gráfico e Legendas
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Estatísticas e Legendas (Lado Esquerdo)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LegendaItemGrafico(
                        cor = Color(0xFF9D8D05),
                        titulo = rotuloSim,
                        quantidade = quantidadeSim,
                        porcentagem = porcentagemSim
                    )
                    LegendaItemGrafico(
                        cor = MaterialTheme.colorScheme.outlineVariant,
                        titulo = rotuloNao,
                        quantidade = quantidadeNao,
                        porcentagem = porcentagemNao
                    )
                }

                // Gráfico em Anel Proporcional Real (Donut Chart) (Lado Direito)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    val sweepAngleSim = (porcentagemSim / 100f) * 360f
                    val sweepAngleNao = 360f - sweepAngleSim
                    val corNao = MaterialTheme.colorScheme.outlineVariant

                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 14.dp.toPx()

                            // Parte "Não" (Pendente / Outros)
                            if (sweepAngleNao > 0f) {
                                drawArc(
                                    color = corNao,
                                    startAngle = -90f + sweepAngleSim,
                                    sweepAngle = sweepAngleNao,
                                    useCenter = false,
                                    style = Stroke(
                                        width = strokeWidth,
                                        cap = StrokeCap.Round
                                    )
                                )
                            }
                            // Parte "Sim" (Destaque principal)
                            if (sweepAngleSim > 0f) {
                                drawArc(
                                    color = corDestaque,
                                    startAngle = -90f,
                                    sweepAngle = sweepAngleSim,
                                    useCenter = false,
                                    style = Stroke(
                                        width = strokeWidth,
                                        cap = StrokeCap.Round
                                    )
                                )
                            }
                        }

                        // Texto central com a porcentagem e total
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${porcentagemSim.toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroBotaoTab(
    texto: String,
    selecionado: Boolean,
    corDestaque: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (selecionado) corDestaque else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val contentColor = if (selecionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = texto,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}

@Composable
private fun LegendaItemGrafico(
    cor: Color,
    titulo: String,
    quantidade: Int,
    porcentagem: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(cor)
        )
        Column {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$quantidade (${String.format("%.1f", porcentagem)}%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
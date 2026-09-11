package com.example.chama.ui.components.painel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
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
import coil.compose.AsyncImage
import com.example.chama.data.entity.Crismando

sealed class TipoSacramentoGrafico(val titulo: String) {
    object Batismo : TipoSacramentoGrafico("Batismo")
    object Certidao : TipoSacramentoGrafico("Certidão")
    object Comunhao : TipoSacramentoGrafico("1ª Comunhão")
}

private enum class TipoFiltroLista {
    NENHUM, SIM, NAO
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Composable
fun CardGraficoSacramentos(
    crismandos: List<Crismando>,
    modifier: Modifier = Modifier,
    corDestaque: Color = Color(0xFF9B8800),
    onCrismandoClick: (Crismando) -> Unit = {}
) {
    var abaSelecionada by remember { mutableStateOf<TipoSacramentoGrafico>(TipoSacramentoGrafico.Batismo) }

    // Controla qual lista está aberta: NENHUM, SIM (positivos) ou NAO (pendentes)
    var filtroListaAtivo by remember { mutableStateOf(TipoFiltroLista.NENHUM) }

    val totalCrismandos = crismandos.size
    val totalBatizados = remember(crismandos) { crismandos.count { it.isBatizado } }
    val totalCertidaoEntregue = remember(crismandos) { crismandos.count { it.certidaoBatismoEntregue } }
    val totalPrimeiraComunhao = remember(crismandos) { crismandos.count { it.temPrimeiraComunhao } }

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

    // Filtra dinamicamente a lista de acordo com a aba e o botão clicado
    val crismandosFiltrados = remember(crismandos, abaSelecionada, filtroListaAtivo) {
        when (filtroListaAtivo) {
            TipoFiltroLista.SIM -> {
                when (abaSelecionada) {
                    is TipoSacramentoGrafico.Batismo -> crismandos.filter { it.isBatizado }
                    is TipoSacramentoGrafico.Certidao -> crismandos.filter { it.certidaoBatismoEntregue }
                    is TipoSacramentoGrafico.Comunhao -> crismandos.filter { it.temPrimeiraComunhao }
                }
            }
            TipoFiltroLista.NAO -> {
                when (abaSelecionada) {
                    is TipoSacramentoGrafico.Batismo -> crismandos.filter { !it.isBatizado }
                    is TipoSacramentoGrafico.Certidao -> crismandos.filter { !it.certidaoBatismoEntregue }
                    is TipoSacramentoGrafico.Comunhao -> crismandos.filter { !it.temPrimeiraComunhao }
                }
            }
            TipoFiltroLista.NENHUM -> emptyList()
        }
    }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vida Sacramental",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (filtroListaAtivo != TipoFiltroLista.NENHUM) {
                        Text(
                            text = "Ocultar lista",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = corDestaque,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { filtroListaAtivo = TipoFiltroLista.NENHUM }
                                .padding(4.dp)
                        )
                    }
                }

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
                        onClick = {
                            abaSelecionada = TipoSacramentoGrafico.Batismo
                            filtroListaAtivo = TipoFiltroLista.NENHUM
                        }
                    )
                    FiltroBotaoTab(
                        texto = "Certidão",
                        selecionado = abaSelecionada is TipoSacramentoGrafico.Certidao,
                        corDestaque = corDestaque,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            abaSelecionada = TipoSacramentoGrafico.Certidao
                            filtroListaAtivo = TipoFiltroLista.NENHUM
                        }
                    )
                    FiltroBotaoTab(
                        texto = "Comunhão",
                        selecionado = abaSelecionada is TipoSacramentoGrafico.Comunhao,
                        corDestaque = corDestaque,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            abaSelecionada = TipoSacramentoGrafico.Comunhao
                            filtroListaAtivo = TipoFiltroLista.NENHUM
                        }
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
                // Estatísticas e Legendas (Lado Esquerdo - Ambas clicáveis)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Legenda SIM Clicável
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (filtroListaAtivo == TipoFiltroLista.SIM) corDestaque.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                filtroListaAtivo = if (filtroListaAtivo == TipoFiltroLista.SIM) TipoFiltroLista.NENHUM else TipoFiltroLista.SIM
                            }
                            .padding(4.dp)
                    ) {
                        LegendaItemGrafico(
                            cor = corDestaque,
                            titulo = "$rotuloSim (Ver ➔)",
                            quantidade = quantidadeSim,
                            porcentagem = porcentagemSim
                        )
                    }

                    // Legenda NÃO Clicável
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (filtroListaAtivo == TipoFiltroLista.NAO) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f) else Color.Transparent)
                            .clickable {
                                filtroListaAtivo = if (filtroListaAtivo == TipoFiltroLista.NAO) TipoFiltroLista.NENHUM else TipoFiltroLista.NAO
                            }
                            .padding(4.dp)
                    ) {
                        LegendaItemGrafico(
                            cor = MaterialTheme.colorScheme.outlineVariant,
                            titulo = "$rotuloNao (Ver ➔)",
                            quantidade = quantidadeNao,
                            porcentagem = porcentagemNao
                        )
                    }
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

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${porcentagemSim.toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$totalCrismandos total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Lista Expansível de Crismandos com base na seleção (SIM ou NÃO)
            AnimatedVisibility(
                visible = filtroListaAtivo != TipoFiltroLista.NENHUM,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    val tituloLista = if (filtroListaAtivo == TipoFiltroLista.SIM) rotuloSim else rotuloNao
                    Text(
                        text = "Crismandos com status: $tituloLista",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (filtroListaAtivo == TipoFiltroLista.SIM) corDestaque else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (crismandosFiltrados.isEmpty()) {
                        Text(
                            text = "Nenhum crismando nesta categoria.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        crismandosFiltrados.sortedBy { it.nome }.forEach { crismando ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onCrismandoClick(crismando) }
                                    .padding(vertical = 6.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!crismando.fotoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = crismando.fotoUrl,
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
                                    text = crismando.nome,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )

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
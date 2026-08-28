package com.example.chama.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.chama.data.entity.Crismando
import com.example.chama.data.entity.Genero
import com.example.chama.ui.MainViewModel
import com.example.chama.ui.components.presencas.DetalhesCrismando
import java.time.LocalDate
import java.time.Period

data class CrismandoFaltasInfo(
    val crismando: Crismando,
    val totalFaltas: Int,
    val totalPresentes: Int,
    val porcentagemPresenca: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPainelGerencial(
    viewModel: MainViewModel,
    onVoltar: () -> Unit = {}
) {
    val crismandos by viewModel.listaCrismandosOriginal.collectAsState()
    val todasPresencas by viewModel.todasPresencas.collectAsState()
    val diasComChamada by viewModel.diasComChamada.collectAsState()
    val listaRifas by viewModel.listaRifas.collectAsState()

    var crismandoDetalhes by remember { mutableStateOf<Crismando?>(null) }

    BackHandler(enabled = crismandoDetalhes != null) {
        crismandoDetalhes = null
    }

    val totalCrismandos = crismandos.size
    val dataDeHoje = viewModel.dataDeHoje

    val datasAteHoje = remember(diasComChamada, dataDeHoje) {
        diasComChamada.filter { runCatching { LocalDate.parse(it) <= dataDeHoje }.getOrDefault(false) }
    }

    val metricasGerais = remember(todasPresencas, datasAteHoje, totalCrismandos) {
        val totalEncontros = datasAteHoje.size
        val totalPossivelPresencas = totalCrismandos * totalEncontros
        val presencasRealizadas = todasPresencas.filter { it.data in datasAteHoje }
        val totalPresentes = presencasRealizadas.count { it.estaPresente }
        val totalFaltas = (totalPossivelPresencas - totalPresentes).coerceAtLeast(0)
        val porcentagemGeral = if (totalPossivelPresencas > 0) {
            (totalPresentes.toFloat() / totalPossivelPresencas) * 100f
        } else {
            100f
        }
        val mediaFaltasPorEncontro = if (totalEncontros > 0) {
            totalFaltas.toFloat() / totalEncontros.toFloat()
        } else {
            0f
        }

        object {
            val encontros = totalEncontros
            val presentes = totalPresentes
            val faltas = totalFaltas
            val porcentagem = porcentagemGeral
            val mediaFaltas = mediaFaltasPorEncontro
        }
    }

    val listaCrismandosComFaltas = remember(crismandos, todasPresencas, datasAteHoje) {
        val totalEncontros = datasAteHoje.size
        crismandos.map { crismando ->
            val presencasDoCrismando = todasPresencas.filter {
                it.crismandoId == crismando.crismandoId && it.data in datasAteHoje
            }
            val presentes = presencasDoCrismando.count { it.estaPresente }
            val faltas = (totalEncontros - presentes).coerceAtLeast(0)
            val porcentagem = if (totalEncontros > 0) (presentes.toFloat() / totalEncontros) * 100f else 100f

            CrismandoFaltasInfo(
                crismando = crismando,
                totalFaltas = faltas,
                totalPresentes = presentes,
                porcentagemPresenca = porcentagem
            )
        }
    }

    val distribuicaoIdades = remember(crismandos) {
        var faixa13_15 = 0
        var faixa16_17 = 0
        var faixa18_20 = 0
        var faixa20_25 = 0
        var faixa26Mais = 0

        for (c in crismandos) {
            val idade = runCatching {
                c.dataNascimento?.let {
                    val birthDate = LocalDate.parse(it)
                    Period.between(birthDate, LocalDate.now()).years
                }
            }.getOrNull() ?: c.idade

            if (idade != null) {
                when {
                    idade in 13..15 -> faixa13_15++
                    idade in 16..17 -> faixa16_17++
                    idade in 18..20 -> faixa18_20++
                    idade in 21..25 -> faixa20_25++
                    idade >= 26 -> faixa26Mais++
                }
            }
        }

        buildMap {
            put("13 - 15 anos", faixa13_15)
            put("16 - 17 anos", faixa16_17)
            put("18 - 20 anos", faixa18_20)
            put("20 - 25 anos", faixa20_25)
            if (faixa26Mais > 0) {
                put("26+ anos", faixa26Mais)
            }
        }
    }

    val totalMeninos = remember(crismandos) {
        crismandos.count { it.genero == Genero.MASCULINO }
    }
    val totalMeninas = remember(crismandos) {
        crismandos.count { it.genero == Genero.FEMININO }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Painel Gerencial", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card Refatorado: Total de Crismandos e Total de Encontros
                CardMetricasCabecalho(
                    totalCrismandos = totalCrismandos,
                    totalEncontros = metricasGerais.encontros
                )

                // Card de Frequência Geral com Média de Faltas
                CardFrequenciaGeral(
                    porcentagemPresenca = metricasGerais.porcentagem,
                    mediaFaltasPorEncontro = metricasGerais.mediaFaltas
                )

                // Top 5 / Todos com Faltas
                CardCrismandosPorFaltas(
                    lista = listaCrismandosComFaltas,
                    onCrismandoClick = { crismandoDetalhes = it }
                )

                // Faixas Etárias
                CardFaixaEtaria(
                    distribuicaoIdades = distribuicaoIdades,
                    total = totalCrismandos
                )

                // Gênero
                CardDistribuicaoGenero(
                    totalMeninos = totalMeninos,
                    totalMeninas = totalMeninas,
                    total = totalCrismandos
                )
            }

            AnimatedVisibility(
                visible = crismandoDetalhes != null,
                enter = scaleIn(initialScale = 0.85f) + fadeIn(),
                exit = scaleOut(targetScale = 0.85f) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                crismandoDetalhes?.let { crismando ->
                    val blocos = remember(listaRifas, crismando) {
                        listaRifas.filter { it.vendedorId == crismando.crismandoId }.map { it.bloco }.distinct()
                    }
                    val presencasDoCrismando = remember(todasPresencas, crismando, datasAteHoje) {
                        todasPresencas.filter { it.crismandoId == crismando.crismandoId && it.data in datasAteHoje }
                    }
                    val totalEncontros = datasAteHoje.size
                    val totalPresentes = presencasDoCrismando.count { it.estaPresente }
                    val totalFaltas = (totalEncontros - totalPresentes).coerceAtLeast(0)
                    val porcentagem = if (totalEncontros > 0) (totalPresentes.toFloat() / totalEncontros) * 100f else 100f

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        DetalhesCrismando(
                            crismando = crismando,
                            blocosVinculados = blocos,
                            totalFaltas = totalFaltas,
                            totalPresentes = totalPresentes,
                            totalEncontrosRealizados = totalEncontros,
                            porcentagemPresenca = porcentagem,
                            onFechar = { crismandoDetalhes = null },
                            onExcluir = { c ->
                                viewModel.excluirCrismando(c.crismandoId)
                                crismandoDetalhes = null
                            },
                            onAtualizar = { crismandoAtualizado ->
                                viewModel.atualizarCrismando(crismandoAtualizado)
                                crismandoDetalhes = crismandoAtualizado
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardMetricasCabecalho(
    totalCrismandos: Int,
    totalEncontros: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Métrica 1: Crismandos
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Crismandos",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$totalCrismandos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Separador vertical sutil
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )

            // Métrica 2: Encontros
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.EventNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Encontros",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$totalEncontros",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun CardFrequenciaGeral(
    porcentagemPresenca: Float,
    mediaFaltasPorEncontro: Float,
    modifier: Modifier = Modifier
) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Frequência Geral da Turma",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Assiduidade Geral",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "%.1f%%".format(porcentagemPresenca),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (porcentagemPresenca >= 75f) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Faltas/Encontro",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "%.1f".format(mediaFaltasPorEncontro),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (porcentagemPresenca / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (porcentagemPresenca >= 75f) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                drawStopIndicator = {}
            )
        }
    }
}

@Composable
fun CardCrismandosPorFaltas(
    lista: List<CrismandoFaltasInfo>,
    onCrismandoClick: (Crismando) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }

    val listaOrdenadaFaltosos = remember(lista) {
        lista.filter { it.totalFaltas > 0 }
            .sortedByDescending { it.totalFaltas }
    }

    val listaExibida = if (expandido) {
        listaOrdenadaFaltosos
    } else {
        listaOrdenadaFaltosos.take(5)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expandido) "Todos com Faltas" else "Top 5 Mais Faltas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (listaOrdenadaFaltosos.size > 5) {
                    TextButton(
                        onClick = { expandido = !expandido },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (expandido) "Ver menos" else "Ver todos (${listaOrdenadaFaltosos.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (listaOrdenadaFaltosos.isEmpty()) {
                Text(
                    text = "Nenhum crismando possui faltas registradas até o momento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listaExibida.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onCrismandoClick(item.crismando) }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!item.crismando.fotoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = item.crismando.fotoUrl,
                                        contentDescription = "Foto de ${item.crismando.nome}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = item.crismando.nome,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "${item.totalFaltas} falta(s)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "(%.0f%%)".format(item.porcentagemPresenca),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Ver detalhes",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (index < listaExibida.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardFaixaEtaria(
    distribuicaoIdades: Map<String, Int>,
    total: Int,
    modifier: Modifier = Modifier
) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Distribuição por Faixa Etária",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            distribuicaoIdades.forEach { (faixa, quantidade) ->
                val porcentagem = if (total > 0) quantidade.toFloat() / total.toFloat() else 0f

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = faixa,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$quantidade (${(porcentagem * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { porcentagem.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        drawStopIndicator = {}
                    )
                }
            }
        }
    }
}

@Composable
fun CardDistribuicaoGenero(
    totalMeninos: Int,
    totalMeninas: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val percMeninos = if (total > 0) totalMeninos.toFloat() / total else 0f
    val percMeninas = if (total > 0) totalMeninas.toFloat() / total else 0f

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Distribuição por Gênero",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Masculino", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "$totalMeninos (${(percMeninos * 100).toInt()}%)",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5)
                    )
                }
                LinearProgressIndicator(
                    progress = { percMeninos },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF1E88E5),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    drawStopIndicator = {}
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Feminino", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "$totalMeninas (${(percMeninas * 100).toInt()}%)",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63)
                    )
                }
                LinearProgressIndicator(
                    progress = { percMeninas },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFE91E63),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    drawStopIndicator = {}
                )
            }
        }
    }
}
package com.example.chama.ui.screens

import com.example.chama.ui.components.gerencial.CardFaixaEtaria
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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.chama.data.entity.Crismando
import com.example.chama.data.entity.Genero
import com.example.chama.ui.MainViewModel
import com.example.chama.ui.components.gerencial.CardCrismandosPorFaltas
import com.example.chama.ui.components.gerencial.CardDistribuicaoGenero
import com.example.chama.ui.components.gerencial.CardFrequenciaGeral
import com.example.chama.ui.components.gerencial.CardMetricasCabecalho
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

                // Top 5 Faltas / Todos com Faltas
                CardCrismandosPorFaltas(
                    lista = listaCrismandosComFaltas,
                    onCrismandoClick = { crismandoDetalhes = it }
                )

                // Faixas Etárias
                CardFaixaEtaria(
                    crismandos = crismandos,
                    onCrismandoClick = { crismandoDetalhes = it }
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




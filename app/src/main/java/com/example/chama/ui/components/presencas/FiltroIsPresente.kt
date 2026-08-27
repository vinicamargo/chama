package com.example.chama.ui.components.presencas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chama.FiltroPresenca
import com.example.chama.ui.MainViewModel

@Composable
fun SeletorDeFiltroPresencaEAcoes(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val filtroSelecionado by viewModel.filtroPresencaSelecionado
    val totalPresentes by viewModel.totalPresentes.collectAsState()
    val totalAusentes by viewModel.totalAusentes.collectAsState()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FiltroBtn(
            label = "Todos",
            icone = null,
            selecionado = filtroSelecionado == FiltroPresenca.TODOS,
            modifier = Modifier.weight(1f) // Divide o espaço igualmente
        ) {
            viewModel.alterarFiltroPresenca(FiltroPresenca.TODOS)
        }

        FiltroBtn(
            label = " ($totalPresentes)",
            icone = Icons.Default.CheckCircle,
            selecionado = filtroSelecionado == FiltroPresenca.PRESENTES,
            modifier = Modifier.weight(1f) // Divide o espaço igualmente
        ) {
            viewModel.alterarFiltroPresenca(FiltroPresenca.PRESENTES)
        }

        FiltroBtn(
            label = " ($totalAusentes)",
            icone = Icons.Default.Close,
            selecionado = filtroSelecionado == FiltroPresenca.AUSENTES,
            modifier = Modifier.weight(1f) // Divide o espaço igualmente
        ) {
            viewModel.alterarFiltroPresenca(FiltroPresenca.AUSENTES)
        }

        // Mantém tamanho fixo
        PresencasExport(viewModel)
    }
}
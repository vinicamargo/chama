package com.example.chama.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chama.FiltroPresenca
import com.example.chama.MainViewModel

@Composable
fun SeletorDeFiltro(
    viewModel: MainViewModel,
    modifier: Modifier
) {
    val filtroSelecionado by viewModel.filtroPresencaAtual

    Row(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        FiltroBtn("Todos", filtroSelecionado == FiltroPresenca.TODOS) {
            viewModel.alterarFiltroPresenca(FiltroPresenca.TODOS)
        }
        FiltroBtn("Presentes", filtroSelecionado == FiltroPresenca.PRESENTES) {
            viewModel.alterarFiltroPresenca(FiltroPresenca.PRESENTES)
        }
        FiltroBtn("Ausentes", filtroSelecionado == FiltroPresenca.AUSENTES) {
            viewModel.alterarFiltroPresenca(FiltroPresenca.AUSENTES)
        }
    }
}

@Composable
fun FiltroBtn(label: String, selecionado: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = 20.dp,
            vertical = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = Color.Black
        )
    ) {
        Text(label)
    }
}
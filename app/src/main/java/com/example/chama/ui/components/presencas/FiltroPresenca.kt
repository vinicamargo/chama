package com.example.chama.ui.components.presencas

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.example.chama.FiltroPresenca
import com.example.chama.ui.MainViewModel
import com.example.chama.utils.PdfPresencaGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun FiltroPresenca(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val filtroSelecionado by viewModel.filtroPresencaSelecionado
    val totalPresentes by viewModel.totalPresentes.collectAsState()
    val totalAusentes by viewModel.totalAusentes.collectAsState()

    val crismandos by viewModel.listaCrismandosOriginal.collectAsState()
    val todasPresencas by viewModel.todasPresencas.collectAsState()
    val diasComChamada by viewModel.diasComChamada.collectAsState()

    val context = LocalContext.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FiltroBtn(
            label = "Todos",
            icone = null,
            selecionado = filtroSelecionado == FiltroPresenca.TODOS,
            modifier = Modifier.weight(1f)
        ) {
            viewModel.alterarFiltroPresenca(FiltroPresenca.TODOS)
        }

        FiltroBtn(
            label = " ($totalPresentes)",
            icone = Icons.Default.CheckCircle,
            selecionado = filtroSelecionado == FiltroPresenca.PRESENTES,
            modifier = Modifier.weight(1f)
        ) {
            viewModel.alterarFiltroPresenca(FiltroPresenca.PRESENTES)
        }

        FiltroBtn(
            label = " ($totalAusentes)",
            icone = Icons.Default.Close,
            selecionado = filtroSelecionado == FiltroPresenca.AUSENTES,
            modifier = Modifier.weight(1f)
        ) {
            viewModel.alterarFiltroPresenca(FiltroPresenca.AUSENTES)
        }

        // Botão de Exportar direto para o Diário de Presenças em PDF
        FilledTonalIconButton(
            onClick = {
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    // Busca as presenças atualizadas diretamente do banco para não usar snapshot defasado
                    val presencasAtualizadas = viewModel.obterTodasPresencasAtualizadas()

                    val pdfFile = PdfPresencaGenerator.gerarPdfPresencas(
                        context = context,
                        crismandos = viewModel.listaCrismandosOriginal.value,
                        diasComChamada = viewModel.diasComChamada.value,
                        todasPresencas = presencasAtualizadas,
                        dataLimite = viewModel.dataDeHoje
                    )

                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        pdfFile
                    )

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartilhar Diário de Presenças"))
                }
            },
            shape = RoundedCornerShape(6.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color(0x9C1C6914),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Compartilhar Diário de Presenças em PDF",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
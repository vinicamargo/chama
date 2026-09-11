package com.example.chama.ui.components.presencas

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    var mostrarOpcoesExport by remember { mutableStateOf(false) }

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

        // Botão que abre as opções de exportação
        FilledTonalIconButton(
            onClick = { mostrarOpcoesExport = true },
            shape = RoundedCornerShape(6.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color(0x9C1C6914),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Exportar Relatórios",
                modifier = Modifier.size(18.dp)
            )
        }
    }

    // Modal de escolha de exportação
    if (mostrarOpcoesExport) {
        AlertDialog(
            onDismissRequest = { mostrarOpcoesExport = false },
            title = {
                Text(
                    text = "Exportar Documentos",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Selecione o documento que deseja gerar e compartilhar:",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = {
                            mostrarOpcoesExport = false
                            viewModel.viewModelScope.launch(Dispatchers.IO) {
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
                                context.startActivity(
                                    Intent.createChooser(intent, "Compartilhar Diário de Presenças")
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Diário de Presenças (PDF)")
                    }

                    // Opção 2: Fichas Cadastrais dos Crismandos (HTML/Impressão)
                    Button(
                        onClick = {
                            mostrarOpcoesExport = false
                            viewModel.exportarFichasPdf(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Fichas Cadastrais (PDF)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarOpcoesExport = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
package com.example.chama.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chama.ui.MainViewModel
import com.example.chama.ui.components.ListaVendedoresSheet
import com.example.chama.ui.components.RifaCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaRifas(
    viewModel: MainViewModel
)
{
    val listaRifas by viewModel.listaRifas.collectAsState()
    val rifaSelecionada by viewModel.rifaSelecionada

    var showSheet by remember { mutableStateOf(false) }
    var conteudoSheet by remember { mutableStateOf(TipoConteudoSheet.ACOES) }

    Box(modifier = Modifier.fillMaxSize())
    {
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            items(listaRifas.distinctBy {it.bloco} ){ primeiraRifaBloco ->
                RifaCard(
                    viewModel = viewModel,
                    primeiraRifaBloco = primeiraRifaBloco,
                    isBlocoSelecionado = rifaSelecionada?.bloco == primeiraRifaBloco.bloco,
                    onClick = { viewModel.selecionarRifa(primeiraRifaBloco) },
                    onAlterar = { showSheet = true }
                )
            }
        }

        if (showSheet) {
            ModalBottomSheet(onDismissRequest = {
                showSheet = false
                conteudoSheet = TipoConteudoSheet.ACOES
            }) {
                when (conteudoSheet) {
                    TipoConteudoSheet.ACOES -> {
                        OpcoesBlocoSheet(
                            onAlterarVendedor = { conteudoSheet = TipoConteudoSheet.SELECAO_VENDEDOR },
                            onAlterarStatus = { conteudoSheet = TipoConteudoSheet.SELECAO_ESTADO },
                            onMarcarComoPago = { /* Lógica direta no ViewModel */ showSheet = false }
                        )
                    }
                    TipoConteudoSheet.SELECAO_VENDEDOR -> {
                        rifaSelecionada?.let {rifa ->
                            ListaVendedoresSheet(
                                viewModel = viewModel,
                                onVendedorSelected = { v ->
                                    viewModel.vincularVendedorAoBloco(v, rifa.bloco)
                                    viewModel.selecionarRifa(null)
                                    conteudoSheet = TipoConteudoSheet.ACOES
                                    showSheet = false
                                })
                        }
                    }
                    TipoConteudoSheet.SELECAO_ESTADO -> {
                        Text("HAHAHAHAH")
//                        ListaEstadosSheet(onEstadoSelected = { status ->
//                            viewModel.atualizarStatus(status, rifaSelecionada?.bloco)
//                            showSheet = false
//                        })
                    }
                }
            }
        }
    }
}

@Composable
fun OpcoesBlocoSheet(
    onAlterarVendedor: () -> Unit,
    onAlterarStatus: () -> Unit,
    onMarcarComoPago: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        Text(
            text = "Gerenciar Bloco",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 22.dp)
        )

        // Opção 1: Vendedor
        ItemOpcaoSheet(
            titulo = "Alterar Vendedor",
            subtitulo = "Vincular crismando, catequista ou vendedor externo",
            onClick = onAlterarVendedor
        )

        // Opção 2: Status
        ItemOpcaoSheet(
            titulo = "Alterar Estado da Rifa",
            subtitulo = "Pendente, Entregue ou Devolvido",
            onClick = onAlterarStatus
        )

        // Opção 3: Atalho de Pagamento
        ItemOpcaoSheet(
            titulo = "Marcar como Pago",
            subtitulo = "Registrar entrada do valor integral",
            onClick = onMarcarComoPago
        )
    }
}

@Composable
fun ItemOpcaoSheet(titulo: String, subtitulo: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitulo, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

enum class TipoConteudoSheet { ACOES, SELECAO_VENDEDOR, SELECAO_ESTADO }

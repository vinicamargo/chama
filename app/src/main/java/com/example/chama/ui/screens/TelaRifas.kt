package com.example.chama.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chama.ui.MainViewModel
import com.example.chama.ui.components.rifas.sheet.ListaVendedoresSheet
import com.example.chama.ui.components.rifas.sheet.MenuSheet
import com.example.chama.ui.components.rifas.RifaCard
import com.example.chama.utils.TipoVendedor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaRifas(
    viewModel: MainViewModel
)
{
    val listaRifas by viewModel.listaRifas.collectAsState()
    val rifaSelecionada by viewModel.rifaSelecionada

    var fabExpandido by remember { mutableStateOf(false) }

    var showNovoVendedorDialog by remember { mutableStateOf(false) }
    var nomeNovoVendedor by remember { mutableStateOf("") }
    var expandedTipoVendedor by remember { mutableStateOf(false) }
    var tipoSelecionado by remember { mutableStateOf(TipoVendedor.COLABORADOR)}

    var showSheet by remember { mutableStateOf(false) }
    var conteudoSheet by remember { mutableStateOf(TipoConteudoSheet.ACOES) }

    var shouldShowRemovalDialog by remember { mutableStateOf(false) }
    var shouldShowAlternarPagamentoDialog by remember { mutableStateOf(false) }


    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (fabExpandido) {
                    SmallFloatingActionButton(
                        onClick = {
                            fabExpandido = false
                            showNovoVendedorDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.offset(x = (-4).dp)
                        ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Novo vendedor")
                        }
                    }

                    // TODO: Implementar export de rifas
//                    SmallFloatingActionButton(
//                        onClick = {
//                            fabExpandido = false
//                            /* sua ação aqui */
//                        },
//                        shape = RectangleShape,
//                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
//                        modifier = Modifier.offset(x = (-16).dp)
//                    ) {
//                        Icon(Icons.Default.Share, contentDescription = null)
//                    }
                }

                FloatingActionButton(
                    onClick = { fabExpandido = !fabExpandido },
                    shape = RoundedCornerShape(12.dp),
                    containerColor = if (fabExpandido) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(52.dp)
                        .offset(x = (-4).dp)
                ) {
                    Icon(
                        imageVector = if (fabExpandido) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Menu"
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding))
        {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 12.dp, end = 12.dp,
                        top = 8.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                items(listaRifas.distinctBy { it.bloco }) { primeiraRifaBloco ->
                    RifaCard(
                        viewModel = viewModel,
                        primeiraRifaBloco = primeiraRifaBloco,
                        isBlocoSelecionado = rifaSelecionada?.bloco == primeiraRifaBloco.bloco,
                        onClick = { viewModel.selecionarRifa(primeiraRifaBloco) },
                        onAlterar = { showSheet = true }
                    )
                }
            }
        }

        if (showNovoVendedorDialog) {
            AlertDialog(
                onDismissRequest = {
                    showNovoVendedorDialog = false
                    nomeNovoVendedor = ""
                },
                title = {
                    Text(text = "Novo Vendedor")
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Digite o nome do novo vendedor que deseja cadastrar:")
                        OutlinedTextField(
                            value = nomeNovoVendedor,
                            onValueChange = { nomeNovoVendedor = it },
                            label = { Text("Nome completo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = expandedTipoVendedor,
                            onExpandedChange = { expandedTipoVendedor = !expandedTipoVendedor },
                        ) {
                            OutlinedTextField(
                                value = tipoSelecionado.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipoVendedor) },
                                modifier = Modifier
                                    .menuAnchor(
                                        type = MenuAnchorType.PrimaryNotEditable,
                                        enabled = true
                                    )
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = expandedTipoVendedor,
                                onDismissRequest = { expandedTipoVendedor = false }
                            ) {
                                TipoVendedor.entries.forEach { opcao ->
                                    if (opcao != TipoVendedor.CRISMANDO){
                                        DropdownMenuItem(
                                            text = { Text(opcao.name) },
                                            onClick = {
                                                tipoSelecionado = opcao
                                                expandedTipoVendedor = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (nomeNovoVendedor.isNotBlank()) {
                                viewModel.registrarVendedor(nomeNovoVendedor, tipoSelecionado)
                                showNovoVendedorDialog = false
                                nomeNovoVendedor = ""
                            }
                        }
                    ) {
                        Text("Salvar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showNovoVendedorDialog = false
                        nomeNovoVendedor = ""
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showSheet) {
            ModalBottomSheet(onDismissRequest = {
                showSheet = false
                conteudoSheet = TipoConteudoSheet.ACOES
                viewModel.alterarFiltroNome("")
            }) {
                when (conteudoSheet) {
                    TipoConteudoSheet.ACOES -> {
                        MenuSheet(
                            viewModel = viewModel,
                            onAlterarVendedor = { conteudoSheet = TipoConteudoSheet.SELECAO_VENDEDOR },
                            onRemoverVendedor = { shouldShowRemovalDialog = true },
                            onAlternarIsPago = {  shouldShowAlternarPagamentoDialog = true },
                            rifaSelecionada = rifaSelecionada!!
                        )
                    }
                    TipoConteudoSheet.SELECAO_VENDEDOR -> {
                        rifaSelecionada?.let {rifa ->
                            ListaVendedoresSheet(
                                viewModel = viewModel,
                                onVendedorSelecionado = { v ->
                                    v?.let {
                                        viewModel.vincularVendedorAoBloco(v, rifa.bloco)
                                    } ?: run {
                                        viewModel.desvincularVendedorDoBloco(rifa.bloco)
                                    }
                                    viewModel.selecionarRifa(null)
                                    conteudoSheet = TipoConteudoSheet.ACOES
                                    showSheet = false
                                    viewModel.alterarFiltroNome("")
                                })
                        }
                    }
                }
            }
        }

        if (shouldShowRemovalDialog) {
            AlertDialog(
                onDismissRequest = { shouldShowRemovalDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.desvincularVendedorDoBloco(rifaSelecionada?.bloco ?: 0)
                        shouldShowRemovalDialog = false; showSheet = false; viewModel.selecionarRifa(null)
                    }) {
                        Text("Confirmar", color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { shouldShowRemovalDialog = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Confirmar remoção de vendedor") },
                text = { Text("Deseja remover o vendedor vinculado ao bloco?") }
            )
        }

        if (shouldShowAlternarPagamentoDialog) {

            var title = ""
            var text = ""

            if (rifaSelecionada?.estaPaga == true){
                    title = "Cancelar confirmação de pagamento"
                    text = "Deseja cancelar a confirmação de pagamento para o bloco?"
            } else {
                    title = "Confirmar pagamento"
                    text = "Deseja confirmar o pagamento para o bloco?"
            }

            AlertDialog(
                onDismissRequest = { shouldShowAlternarPagamentoDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.alternarPagamentoRifa(rifaSelecionada!!)
                        shouldShowAlternarPagamentoDialog = false; showSheet = false; viewModel.selecionarRifa(null)
                    }) {
                        Text("Confirmar", color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { shouldShowAlternarPagamentoDialog = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text(title) },
                text = { Text(text) }
            )
        }
    }
}

enum class TipoConteudoSheet { ACOES, SELECAO_VENDEDOR }

package com.example.chama.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.example.chama.ui.MainViewModel
import com.example.chama.ui.components.rifas.DialogConfirmarExclusaoBlocosEmUso
import com.example.chama.ui.components.rifas.DialogExcluirUltimosBlocos
import com.example.chama.ui.components.rifas.DialogInserirBlocosEmLote
import com.example.chama.ui.components.rifas.RifaCard
import com.example.chama.ui.components.rifas.sheet.ListaVendedoresSheet
import com.example.chama.ui.components.rifas.sheet.MenuSheet
import com.example.chama.utils.TipoVendedor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaRifas(
    viewModel: MainViewModel
) {
    val listaRifas by viewModel.listaRifas.collectAsState()
    val rifaSelecionada by viewModel.rifaSelecionada

    var fabExpandido by remember { mutableStateOf(false) }

    var showNovoVendedorDialog by remember { mutableStateOf(false) }
    var nomeNovoVendedor by remember { mutableStateOf("") }
    var expandedTipoVendedor by remember { mutableStateOf(false) }
    var tipoSelecionado by remember { mutableStateOf(TipoVendedor.COLABORADOR) }

    var showAcoesSheet by remember { mutableStateOf(false) }
    var conteudoSheet by remember { mutableStateOf(TipoConteudoSheet.ACOES) }

    var shouldShowRemovalDialog by remember { mutableStateOf(false) }
    var shouldShowAlternarPagamentoDialog by remember { mutableStateOf(false) }

    // Estados para gerenciamento de Blocos em Lote
    var showDialogInserirBlocos by remember { mutableStateOf(false) }
    var showDialogExcluirBlocos by remember { mutableStateOf(false) }
    var alertaRifasEmUso by remember { mutableStateOf<Pair<Int, Int>?>(null) } // (qtdBlocos, rifasEmUso)

    val totalBlocos = remember(listaRifas) { listaRifas.distinctBy { it.bloco }.size }

    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (fabExpandido) {
                    // 1. Gerar Blocos
                    SmallFloatingActionButton(
                        onClick = {
                            fabExpandido = false
                            showDialogInserirBlocos = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.offset(x = (-4).dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Gerar blocos")
                        }
                    }

                    // 2. Remover Últimos Blocos (visível apenas se houver blocos existentes)
                    if (totalBlocos > 0) {
                        SmallFloatingActionButton(
                            onClick = {
                                fabExpandido = false
                                showDialogExcluirBlocos = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.offset(x = (-4).dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null)
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text("Remover blocos")
                            }
                        }
                    }

                    // 3. Novo Vendedor
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

                    // 4. Exportar CSV
                    SmallFloatingActionButton(
                        onClick = {
                            viewModel.viewModelScope.launch(Dispatchers.IO) {
                                val dadosCsv = viewModel.exportarRifasCSV()

                                val file = File(context.cacheDir, "relatorio_rifas.csv")
                                file.writeText(dadosCsv, charset = Charsets.UTF_8)

                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )

                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Exportar Rifas"))
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.offset(x = (-4).dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Exportar")
                        }
                    }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (listaRifas.isEmpty()) {
                EstadoVazioRifas(
                    onGerarPrimeirasRifas = { showDialogInserirBlocos = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp, end = 12.dp, top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(listaRifas.distinctBy { it.bloco }) { primeiraRifaBloco ->
                        RifaCard(
                            viewModel = viewModel,
                            primeiraRifaBloco = primeiraRifaBloco,
                            isBlocoSelecionado = rifaSelecionada?.bloco == primeiraRifaBloco.bloco,
                            onClick = { viewModel.selecionarRifa(primeiraRifaBloco) },
                            onAlterar = { showAcoesSheet = true }
                        )
                    }
                }
            }
        }

        // Diálogo para Gerar Blocos
        if (showDialogInserirBlocos) {
            DialogInserirBlocosEmLote(
                onDismiss = { showDialogInserirBlocos = false },
                onConfirmar = { qtdBlocos ->
                    viewModel.gerarBlocosEmLote(qtdBlocos)
                }
            )
        }

        // Diálogo para Excluir Últimos Blocos
        if (showDialogExcluirBlocos) {
            DialogExcluirUltimosBlocos(
                totalBlocosExistentes = totalBlocos,
                onDismiss = { showDialogExcluirBlocos = false },
                onConfirmar = { qtdBlocos ->
                    viewModel.excluirUltimosBlocos(
                        quantidadeBlocos = qtdBlocos,
                        forcar = false
                    ) { sucesso, emUso ->
                        if (!sucesso) {
                            alertaRifasEmUso = Pair(qtdBlocos, emUso)
                        }
                    }
                }
            )
        }

        // Diálogo de Alerta de Segurança (quando há rifas pagas/vinculadas nos blocos)
        alertaRifasEmUso?.let { (qtdBlocos, emUso) ->
            DialogConfirmarExclusaoBlocosEmUso(
                quantidadeBlocos = qtdBlocos,
                quantidadeRifasEmUso = emUso,
                onDismiss = { alertaRifasEmUso = null },
                onConfirmarForcar = {
                    viewModel.excluirUltimosBlocos(
                        quantidadeBlocos = qtdBlocos,
                        forcar = true
                    )
                }
            )
        }

        // Diálogo Novo Vendedor
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
                                    if (opcao != TipoVendedor.CRISMANDO) {
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

        // BottomSheet de Ações / Seleção de Vendedor
        if (showAcoesSheet) {
            ModalBottomSheet(onDismissRequest = {
                showAcoesSheet = false
                conteudoSheet = TipoConteudoSheet.ACOES
                viewModel.alterarFiltroNome("")
            }) {
                when (conteudoSheet) {
                    TipoConteudoSheet.ACOES -> {
                        MenuSheet(
                            viewModel = viewModel,
                            onAlterarVendedor = { conteudoSheet = TipoConteudoSheet.SELECAO_VENDEDOR },
                            onRemoverVendedor = { shouldShowRemovalDialog = true },
                            onAlternarIsPago = { shouldShowAlternarPagamentoDialog = true },
                            rifaSelecionada = rifaSelecionada!!
                        )
                    }
                    TipoConteudoSheet.SELECAO_VENDEDOR -> {
                        rifaSelecionada?.let { rifa ->
                            ListaVendedoresSheet(
                                viewModel = viewModel,
                                bloco = rifa.bloco,
                                onVendedorSelecionado = { v ->
                                    v?.let {
                                        viewModel.vincularVendedorAoBloco(v, rifa.bloco)
                                    } ?: run {
                                        viewModel.desvincularVendedorDoBloco(rifa.bloco)
                                    }
                                    viewModel.selecionarRifa(null)
                                    conteudoSheet = TipoConteudoSheet.ACOES
                                    showAcoesSheet = false
                                    viewModel.alterarFiltroNome("")
                                }
                            )
                        }
                    }
                }
            }
        }

        // Diálogo Confirmar Remoção de Vendedor
        if (shouldShowRemovalDialog) {
            AlertDialog(
                onDismissRequest = { shouldShowRemovalDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.desvincularVendedorDoBloco(rifaSelecionada?.bloco ?: 0)
                        shouldShowRemovalDialog = false
                        showAcoesSheet = false
                        viewModel.selecionarRifa(null)
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

        // Diálogo Alternar Pagamento
        if (shouldShowAlternarPagamentoDialog) {
            val title = if (rifaSelecionada?.estaPaga == true) {
                "Cancelar confirmação de pagamento"
            } else {
                "Confirmar pagamento"
            }
            val text = if (rifaSelecionada?.estaPaga == true) {
                "Deseja cancelar a confirmação de pagamento para o bloco?"
            } else {
                "Deseja confirmar o pagamento para o bloco?"
            }

            AlertDialog(
                onDismissRequest = { shouldShowAlternarPagamentoDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.alternarPagamentoRifa(rifaSelecionada!!)
                        shouldShowAlternarPagamentoDialog = false
                        showAcoesSheet = false
                        viewModel.selecionarRifa(null)
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

@Composable
fun EstadoVazioRifas(onGerarPrimeirasRifas: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ConfirmationNumber,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhuma rifa cadastrada",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Você pode gerar os primeiros blocos de rifas para a turma agora.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onGerarPrimeirasRifas,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gerar Rifas Iniciais")
        }
    }
}
package com.example.chama.ui.screens

import android.graphics.drawable.shapes.RoundRectShape
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.chama.data.entity.Rifa
import com.example.chama.ui.MainViewModel
import com.example.chama.ui.components.ListaVendedoresSheet
import com.example.chama.ui.components.RifaCard
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

    var showDialog by remember { mutableStateOf(false) }

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
                    nomeNovoVendedor = "" // Limpa ao fechar
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
                            shape = RoundedCornerShape(12.dp) // Mantendo o padrão curvado que você curtiu
                        )

                        ExposedDropdownMenuBox(
                            expanded = expandedTipoVendedor,
                            onExpandedChange = { expandedTipoVendedor = !expandedTipoVendedor },
                        ) {
                            OutlinedTextField(
                                value = tipoSelecionado.name, // Mostra o nome do Enum selecionado
                                onValueChange = {},
                                readOnly = true, // Usuário não digita aqui
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
                        OpcoesBlocoSheet(
                            viewModel = viewModel,
                            onAlterarVendedor = { conteudoSheet = TipoConteudoSheet.SELECAO_VENDEDOR },
                            onRemoverVendedor = { showDialog = true },
                            onAlterarStatus = { conteudoSheet = TipoConteudoSheet.SELECAO_ESTADO },
                            onMarcarComoPago = { /* Lógica direta no ViewModel */ showSheet = false },
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

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.desvincularVendedorDoBloco(rifaSelecionada?.bloco ?: 0)
                        showDialog = false; showSheet = false; viewModel.selecionarRifa(null)
                    }) {
                        Text("Confirmar", color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Confirmar remoção de vendedor") },
                text = { Text("Deseja remover o vendedor vinculado ao bloco?") }
            )
        }
    }
}

@Composable
fun OpcoesBlocoSheet(
    viewModel: MainViewModel,
    onAlterarVendedor: () -> Unit,
    onRemoverVendedor: () -> Unit,
    onAlterarStatus: () -> Unit,
    onMarcarComoPago: () -> Unit,
    rifaSelecionada: Rifa
) {
    val mapaNomeVendedores by viewModel.mapaNomeVendedores.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(bottom = 14.dp)
    ) {
        Column(
           modifier = Modifier.fillMaxWidth(),
           horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Gerenciar bloco ${rifaSelecionada.bloco}",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = mapaNomeVendedores[rifaSelecionada.vendedorId]?.let { "Vendedor(a): $it"
                } ?: "Sem vendedor",
                modifier = Modifier.padding(top = 12.dp),
                textAlign = TextAlign.Center
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
        )

        // Opção 1: Vendedor
        ItemOpcaoSheet(
            titulo = "Alterar vendedor",
            subtitulo = "Vincular crismando, catequista ou vendedor externo",
            onClick = onAlterarVendedor
        )

        ItemOpcaoSheet(
            titulo = "Remover vendedor",
            subtitulo = "Remover vendedor vinculado",
            onClick = onRemoverVendedor
        )

        // Opção 3: Status
//        ItemOpcaoSheet(
//            titulo = "Alterar Estado da Rifa",
//            subtitulo = "Pendente, Entregue ou Devolvido",
//            onClick = onAlterarStatus
//        )

        // Opção 4: Atalho de Pagamento
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

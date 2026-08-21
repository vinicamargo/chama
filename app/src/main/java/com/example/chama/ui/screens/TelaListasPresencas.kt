package com.example.chama.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chama.FiltroPresenca
import com.example.chama.data.entity.Crismando
import com.example.chama.ui.MainViewModel
import com.example.chama.ui.components.presencas.ConfirmacaoBottomCard
import com.example.chama.ui.components.presencas.CrismandoCard
import com.example.chama.ui.components.presencas.DetalhesCrismandoExpandido
import com.example.chama.ui.components.presencas.SeletorDeFiltroData
import com.example.chama.ui.components.presencas.SeletorDeFiltroPresencaEAcoes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.chama.utils.DataVisualTransformation
import kotlin.random.Random

@Composable
fun TelaListasPresencas(viewModel: MainViewModel) {
    val listaCrismandosFiltrada by viewModel.listaCrismandosFiltrada.collectAsState()
    val presencas by viewModel.presencasDoDia.collectAsState()
    val textoBusca by viewModel.filtroNomeSelecionado
    val crismandoSelecionado by viewModel.crismandoSelecionado
    val filtroPresenca by viewModel.filtroPresencaSelecionado
    val dataFiltrada by viewModel.diaSelecionado.collectAsState()

    var fabExpandido by remember { mutableStateOf(false) }

    var showNovoCrismandoDialog by remember { mutableStateOf(false) }
    var nomeNovoCrismando by remember { mutableStateOf("") }
    var dataNascNovoCrismando by remember { mutableStateOf("") }
    var telefoneNovoCrismando by remember { mutableStateOf("") }
    var responsavelNovoCrismando by remember { mutableStateOf("") }
    var telResponsavelNovoCrismando by remember { mutableStateOf("") }

    fun limparCamposCadastro() {
        nomeNovoCrismando = ""
        dataNascNovoCrismando = ""
        telefoneNovoCrismando = ""
        responsavelNovoCrismando = ""
        telResponsavelNovoCrismando = ""
    }

    val isCrismandoSelecionadoPresente = remember(crismandoSelecionado, presencas) {
        val estaPresente = presencas.find {
            it.crismandoId == crismandoSelecionado?.crismandoId
        }?.estaPresente ?: false
        estaPresente
    }

    var crismandoDetalhes by remember { mutableStateOf<Crismando?>(null) }

    BackHandler(enabled = crismandoDetalhes != null || crismandoSelecionado != null) {
        when {
            crismandoDetalhes != null -> crismandoDetalhes = null
            crismandoSelecionado != null -> viewModel.selecionarCrismando(null)
        }
    }

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = crismandoSelecionado == null && crismandoDetalhes == null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (fabExpandido) {
                        SmallFloatingActionButton(
                            onClick = {
                                showNovoCrismandoDialog = true
                                fabExpandido = false
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
                                Text("Novo crismando")
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Camada 1: Conteúdo Principal da Tela
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SeletorDeFiltroData(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                SeletorDeFiltroPresencaEAcoes(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth()
                )

                if (filtroPresenca == FiltroPresenca.TODOS) {
                    OutlinedTextField(
                        value = textoBusca,
                        onValueChange = { viewModel.alterarFiltroNome(it) },
                        label = { Text("Filtrar por nome") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (textoBusca.isNotEmpty()) {
                                IconButton(onClick = { viewModel.alterarFiltroNome("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpar campo"
                                    )
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(listaCrismandosFiltrada) { crismando ->
                        val presencaCrismando = presencas.find { it.crismandoId == crismando.crismandoId }?.estaPresente

                        CrismandoCard(
                            crismando = crismando,
                            estaPresente = presencaCrismando,
                            selecionado = crismando == crismandoSelecionado,
                            onClick = {
                                viewModel.selecionarCrismando(crismando)
                                fabExpandido = false
                            },
                            onInfoClick = {
                                crismandoDetalhes = crismando
                                fabExpandido = false
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = crismandoSelecionado != null,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                ConfirmacaoBottomCard(
                    isCrismandoPresente = isCrismandoSelecionadoPresente,
                    nome = crismandoSelecionado?.nome,
                    onConfirmar = {
                        crismandoSelecionado?.let {
                            viewModel.alternarPresenca(it.crismandoId, dataFiltrada)
                            viewModel.selecionarCrismando(null)
                            viewModel.alterarFiltroPresenca(FiltroPresenca.TODOS)
                        }
                    },
                    onCancelar = { viewModel.selecionarCrismando(null) }
                )
            }

            AnimatedVisibility(
                visible = crismandoDetalhes != null,
                enter = scaleIn(initialScale = 0.85f) + fadeIn(),
                exit = scaleOut(targetScale = 0.85f) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                crismandoDetalhes?.let { crismando ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        DetalhesCrismandoExpandido(
                            crismando = crismando,
                            onFechar = { crismandoDetalhes = null }
                        )
                    }
                }
            }
        }

        if (showNovoCrismandoDialog) {
            AlertDialog(
                onDismissRequest = {
                    showNovoCrismandoDialog = false
                    limparCamposCadastro()
                },
                title = {
                    Text(text = "Novo Crismando", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = nomeNovoCrismando,
                            onValueChange = { nomeNovoCrismando = it },
                            label = { Text("Nome completo *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = dataNascNovoCrismando,
                            onValueChange = { input ->
                                dataNascNovoCrismando = input.filter { it.isDigit() }.take(8)
                            },
                            label = { Text("Data de Nascimento") },
                            placeholder = { Text("DD/MM/AAAA") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = DataVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = telefoneNovoCrismando,
                            onValueChange = { input ->
                                telefoneNovoCrismando = input.filter { it.isDigit() }.take(11)
                            },
                            label = { Text("Telefone (apenas números)") },
                            placeholder = { Text("Ex: 11987654321") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), // ⬅️ Teclado telefônico
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        OutlinedTextField(
                            value = responsavelNovoCrismando,
                            onValueChange = { responsavelNovoCrismando = it },
                            label = { Text("Nome do Responsável") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = telResponsavelNovoCrismando,
                            onValueChange = { input ->
                                telResponsavelNovoCrismando = input.filter { it.isDigit() }.take(11)
                            },
                            label = { Text("Telefone do Responsável") },
                            placeholder = { Text("Ex: 11987654321") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), // ⬅️ Teclado telefônico
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (nomeNovoCrismando.isNotBlank()) {
                                val dataIso = runCatching {
                                    if (dataNascNovoCrismando.length == 8) {
                                        val dtf = DateTimeFormatter.ofPattern("ddMMyyyy")
                                        LocalDate.parse(dataNascNovoCrismando, dtf).toString()
                                    } else {
                                        null
                                    }
                                }.getOrNull()

                                viewModel.registrarCrismando(
                                    Crismando(
                                        crismandoId = 0L,
                                        nome = nomeNovoCrismando.trim(),
                                        fotoUrl = null,
                                        dataNascimento = dataIso,
                                        telefone = telefoneNovoCrismando.trim().ifBlank { null },
                                        nomeResponsavel = responsavelNovoCrismando.trim().ifBlank { null },
                                        telefoneResponsavel = telResponsavelNovoCrismando.trim().ifBlank { null }
                                    )
                                )
                                showNovoCrismandoDialog = false
                                limparCamposCadastro()
                            }
                        }
                    ) {
                        Text("Salvar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showNovoCrismandoDialog = false
                            limparCamposCadastro()
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
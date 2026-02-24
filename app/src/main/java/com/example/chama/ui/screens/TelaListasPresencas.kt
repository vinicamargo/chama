package com.example.chama.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chama.FiltroPresenca
import com.example.chama.MainViewModel
import com.example.chama.ui.components.ConfirmacaoBottomCard
import com.example.chama.ui.components.CrismandoCard
import com.example.chama.ui.components.SeletorDeFiltroData
import com.example.chama.ui.components.SeletorDeFiltroPresenca

@Composable
fun TelaListasPresencas(viewModel: MainViewModel) {
    val listaCrismandosFiltrada by viewModel.listaCrismandosFiltrada.collectAsState()
    val presencas by viewModel.presencasDoDia.collectAsState()
    val textoBusca by viewModel.filtroNomeSelecionado
    val crismandoSelecionado by viewModel.crismandoSelecionado
    val filtroPresenca by viewModel.filtroPresencaSelecionado
    val dataFiltrada by viewModel.diaSelecionado.collectAsState()


    val isCrismandoSelecionadoPresente = remember(crismandoSelecionado, presencas) {
        val estaPresente = presencas.find {
            it.crismandoId == crismandoSelecionado?.crismandoId
        }?.estaPresente ?: false
        estaPresente
    }

    Box(modifier = Modifier.fillMaxSize()){
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {

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

            Spacer(modifier = Modifier.height(12.dp))

            SeletorDeFiltroData(
                viewModel,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            SeletorDeFiltroPresenca(
                viewModel,
                modifier = Modifier.fillMaxWidth()
            )


            LazyColumn {
                items(listaCrismandosFiltrada) { crismando ->
                    val presencaCrismando = presencas.find { it.crismandoId == crismando.crismandoId}?.estaPresente

                    CrismandoCard(
                        crismando,
                        estaPresente = presencaCrismando,
                        selecionado = crismando == crismandoSelecionado,
                        onClick = {viewModel.selecionarCrismando(crismando)}
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
    }
}

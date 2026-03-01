package com.example.chama.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.chama.data.model.PessoaVendedora
import com.example.chama.ui.MainViewModel

@Composable
fun ListaVendedoresSheet(
    viewModel: MainViewModel,
    onVendedorSelecionado: (Long?) -> Unit
){

    val vendedores by viewModel.listaVendedoresFiltrados.collectAsState()
    val textoBusca by viewModel.filtroNomeSelecionado

    var showDialog by remember { mutableStateOf(false) }
    var vendedorSelecionadoParaConfirmar by remember { mutableStateOf<PessoaVendedora?>(null) }

    var textoDialog by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ){
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

        Spacer(modifier = Modifier.padding(vertical = 8.dp))

        LazyColumn{
            items(vendedores){ v ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        .clickable {
                            vendedorSelecionadoParaConfirmar = v
                            showDialog = true
                        }
                ) {
                    Text(
                        v.nome,
                        modifier = Modifier.padding(vertical = 8.dp).padding(start = 12.dp)
                    )
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        onVendedorSelecionado(vendedorSelecionadoParaConfirmar!!.id)
                        showDialog = false
                    }) {
                        Text("Confirmar", color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Confirmar alteração de vendedor") },
                text = { Text("Deseja vincular como vendedor ${vendedorSelecionadoParaConfirmar!!.nome} ao bloco?") }
            )
        }
    }

}
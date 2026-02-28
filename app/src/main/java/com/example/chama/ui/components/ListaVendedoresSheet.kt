package com.example.chama.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chama.ui.MainViewModel

@Composable
fun ListaVendedoresSheet(
    viewModel: MainViewModel,
    onVendedorSelected: (Long) -> Unit
){

    val vendedores by viewModel.listaVendedores.collectAsState()

    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ){
        LazyColumn{
            items(vendedores){ v ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        .clickable { onVendedorSelected(v.id) }
                ) {
                    Text(
                        v.nome,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
package com.example.chama.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.chama.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaGestaoEventos(viewModel: MainViewModel) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Gestão de Eventos") }) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Em desenvolvimento")
        }
    }
}
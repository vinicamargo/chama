package com.example.chama.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chama.ui.MainViewModel
import com.example.chama.ui.components.RifaCard

@Composable
fun TelaRifas(
    viewModel: MainViewModel
){
    Box(modifier = Modifier.fillMaxSize())
    {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            RifaCard()
        }
    }
}
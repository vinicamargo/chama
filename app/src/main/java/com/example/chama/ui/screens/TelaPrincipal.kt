package com.example.chama.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chama.R

@Composable
fun TelaPrincipal(
    onIrParaLista: () -> Unit,
    onIrParaExportador: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.mipmap.logo_foreground),
            contentDescription = "Logo do App",
            modifier = Modifier.size(300.dp)
        )
        Text(
            text = "CHAMA 1.0",
            fontSize = 32.sp
        )
        Text(
            text = "Lista de chamada dos crismandos 25/26",
            modifier = Modifier.padding(top = 15.dp),
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onIrParaLista,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Lista de Presença")
        }

        Spacer(modifier = Modifier.padding(vertical = 4.dp))

        Button(
            onClick = onIrParaExportador,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Exportar Lista de Presença")
        }
    }
}
package com.example.chama.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chama.data.entity.Rifa
import com.example.chama.ui.MainViewModel

@Composable
fun RifaCard(
    viewModel: MainViewModel,
    primeiraRifaBloco: Rifa,
    isBlocoSelecionado: Boolean,
    onClick: () -> Unit,
    onAlterar: () -> Unit
){

    val mapaNomeVendedores by viewModel.mapaNomeVendedores.collectAsState()

    val numBloco = primeiraRifaBloco.bloco
    val numRifaMenor = primeiraRifaBloco.numero
    val numRifaMaior = primeiraRifaBloco.numero + 9

    var corFundo = Color(0x5E770606)

    if (primeiraRifaBloco.estaPaga)
        corFundo = Color(0x4A3FA93F)
    else if (primeiraRifaBloco.vendedorId != null)
        corFundo = Color(0x4AAFAF3A)


    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() }
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = corFundo),
        border = if (isBlocoSelecionado) BorderStroke(2.dp, Color(0x59B3B9FF)) else null
        ){

        val nomeVendedor = mapaNomeVendedores[primeiraRifaBloco.vendedorId]

        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Column() {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bloco $numBloco",
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Rifas: $numRifaMenor - $numRifaMaior",
                        modifier = Modifier.padding(end = 16.dp, top = 16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nomeVendedor?.takeIf { it.isNotEmpty() }?.
                        let { "Vendedor(a): ${it.split(" ").take(3).joinToString(" ")}" } ?: "Sem vendedor vinculado",
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                }
                if (isBlocoSelecionado) {

                    HorizontalDivider(color = Color(0x59B3B9FF), thickness = 1.dp)

                    OutlinedButton(
                        onClick = { onAlterar() },
                        modifier = Modifier.fillMaxWidth(),
                        border = null,
                        shape = RectangleShape
                    ) {
                        Text("Alterar", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
package com.example.chama.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chama.R
import com.example.chama.ui.MainViewModel
import com.example.chama.ui.theme.GermaniaOne

@Composable
fun TelaPrincipal(
    viewModel: MainViewModel,
    onIrParaLista: () -> Unit,
    onIrParaRifas: () -> Unit
) {
    val solidRedBackground = Color(0xFF5B0000)

    val buttonBackground = Color(0x33000000)
    val buttonBorderColor = Color(0x66FFFFFF)
    val buttonShape = RoundedCornerShape(26.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(solidRedBackground)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.espirito_santo),
            contentDescription = "Logo do App",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(250.dp)
                .padding(top = 16.dp, bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "CHAMA",
            fontSize = 58.sp,
            textAlign = TextAlign.Center,
            fontFamily = GermaniaOne,
            color = Color.White
        )

        Text(
            text = "Gerenciador da catequese de crisma",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
            fontFamily = GermaniaOne,
            color = Color.White.copy(alpha = 0.95f)
        )

        Text(
            text = "2026/2027",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp, bottom = 32.dp),
            fontFamily = GermaniaOne,
            color = Color.White.copy(alpha = 0.85f)
        )

        Column(
            modifier = Modifier.fillMaxWidth(0.78f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botão 1: Lista de Presença
            Button(
                onClick = onIrParaLista,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                shape = buttonShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(buttonShape)
                    .background(buttonBackground)
                    .border(1.5.dp, buttonBorderColor, buttonShape)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lista de Presença",
                        fontFamily = GermaniaOne,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }

            // Botão 2: Gestão de Rifas
            Button(
                onClick = onIrParaRifas,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                shape = buttonShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(buttonShape)
                    .background(buttonBackground)
                    .border(1.5.dp, buttonBorderColor, buttonShape)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Gestão de rifas",
                        fontFamily = GermaniaOne,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
package com.example.chama.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.example.chama.R
import com.example.chama.ui.MainViewModel
import com.example.chama.ui.theme.GermaniaOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun TelaPrincipal(
    viewModel: MainViewModel,
    onIrParaLista: () -> Unit,
    onIrParaRifas: () -> Unit
) {
    val context = LocalContext.current
    val solidRedBackground = Color(0xFF5B0000)

    val buttonBackground = Color(0x33000000)
    val buttonBorderColor = Color(0x66FFFFFF)
    val buttonShape = RoundedCornerShape(26.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(solidRedBackground)
    ) {
        // Ícone discreto de Export/Backup no topo
        IconButton(
            onClick = {
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    val dadosCsv = viewModel.exportarBackupCompletoCSV()

                    val file = File(context.cacheDir, "backup_geral_chama.csv")
                    file.writeText(dadosCsv, charset = Charsets.UTF_8)

                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartilhar Backup Geral"))
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Exportar Backup Completo",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(28.dp)
            )
        }

        // Conteúdo central da tela
        Column(
            modifier = Modifier
                .fillMaxSize()
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
}
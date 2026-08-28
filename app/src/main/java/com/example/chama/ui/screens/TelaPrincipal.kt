package com.example.chama.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.FileDownload
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
    onIrParaRifas: () -> Unit,
    onIrParaPainelGerencial: () -> Unit
) {
    val context = LocalContext.current
    val solidRedBackground = Color(0xFF5B0000)

    val buttonBackground = Color(0x33000000)
    val buttonBorderColor = Color(0x66FFFFFF)
    val buttonShape = RoundedCornerShape(26.dp)

    // Launcher para selecionar arquivo CSV do backup
    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importarDadosCsv(context, it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(solidRedBackground)
    ) {
        // Ícones de Ações no Canto Superior Direito (Importar e Exportar)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Botão de Importar Backup
            IconButton(
                onClick = {
                    csvPickerLauncher.launch(
                        arrayOf(
                            "text/csv",
                            "text/comma-separated-values",
                            "application/csv",
                            "*/*"
                        )
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Importar Backup CSV",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Botão de Exportar Backup
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
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Exportar Backup CSV",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(24.dp)
                )
            }
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
                    .size(230.dp)
                    .padding(top = 8.dp, bottom = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "CHAMA",
                fontSize = 54.sp,
                textAlign = TextAlign.Center,
                fontFamily = GermaniaOne,
                color = Color.White
            )

            Text(
                text = "Gerenciador da catequese de crisma",
                fontSize = 19.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp),
                fontFamily = GermaniaOne,
                color = Color.White.copy(alpha = 0.95f)
            )

            Text(
                text = "2026/2027",
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 24.dp),
                fontFamily = GermaniaOne,
                color = Color.White.copy(alpha = 0.85f)
            )

            Column(
                modifier = Modifier.fillMaxWidth(0.82f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onIrParaLista,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    shape = buttonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
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

                Button(
                    onClick = onIrParaRifas,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    shape = buttonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
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

                Button(
                    onClick = onIrParaPainelGerencial,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    shape = buttonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(buttonShape)
                        .background(buttonBackground)
                        .border(1.5.dp, buttonBorderColor, buttonShape)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Painel Gerencial",
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